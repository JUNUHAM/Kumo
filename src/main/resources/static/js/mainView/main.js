/**
 * KUMO Map Application
 * 기능: 구글 맵 연동, 클러스터링, GPS 기반 주변 공고 검색, UI 인터랙션
 */

// ============================================================
// [1] 전역 상태 관리 (State Management)
// ============================================================
const AppState = {
    map: null,                // 구글 맵 객체
    markerCluster: null,      // 마커 클러스터 객체
    jobMarkers: [],           // 개별 마커 배열
    isLocationMode: false,    // 내 주변 보기 모드 스위치
    debounceTimer: null,      // 디바운스 타이머
    currentXhr: null,          // 현재 진행 중인 AJAX 요청 (취소용)
    lastBounds: null,
    maskPolygon: null,          // 지도 경계선
    ignoreIdle: false // 🌟 [NEW] 지도가 강제 이동 중일 때 자동 갱신을 막는 스위치
};

// ============================================================
// [2] 초기화 및 이벤트 바인딩 (Init & Events)
// ============================================================
$(document).ready(function() {
    // 바텀 시트 핸들 클릭 이벤트
    $('.sheet-handle').on('click', function() {
        const $sheet = $('#bottomSheet');
        $sheet.toggleClass('active');

        if ($sheet.hasClass('active')) {
            UIManager.closeJobCard();
        }
    });

    // 지도 초기화 (Google Maps API 콜백으로 실행됨)
    window.initMap = MapManager.init;

    $(".btn-close-card").on('click', function () {
        UIManager.closeJobCard();
    })

    $(".nav-item").on('click', function () {
        // 1. UI 활성화 처리
        $('.nav-item').removeClass('active');
        $(this).addClass('active');

        // 2. data-tab 속성 값 읽기
        const tabName = $(this).data('tab');

        // 3. 기능 실행
        UIManager.switchTab(tabName);
    })
});

// ============================================================
// [3] 지도 관리자 (Map Manager)
// ============================================================
const MapManager = {
    init: function() {
        const mapElement = document.getElementById('map');
        if (!mapElement) return;

        const tokyo = { lat: 35.6804, lng: 139.7690 };

        // 1. 현재 브라우저가 다크모드 인지 확인 하기
        const isDark = document.body.classList.contains('dark-mode') || localStorage.getItem('theme') === 'dark';

        // 2. 초기 스타일 결정
        const initialStyle = isDark ? MapStyles.dark : MapStyles.light;

        AppState.map = new google.maps.Map(mapElement, {
            center: tokyo,
            zoom: 10,
            disableDefaultUI: true,
            styles: initialStyle,
            gestureHandling: 'greedy',
            maxZoom: 14
        });

        MapManager.drawMasking();
        MapManager.bindMapEvents();

        // 4. 다크모드 변경 감지기 실행
        MapManager.observeThemeChange();
    },

    bindMapEvents: function() {
        const map = AppState.map;

        // 🌟 [복구] 이 부분(이벤트 리스너)이 빠져 있었습니다!
        // 지도가 멈출 때(idle)마다 실행한다는 명령이 없어서 동작을 안 했던 겁니다.
        map.addListener("idle", () => {

            // 강제로 지도가 이동중 (jobRecent) 에는 idle이 실행되지 않도록 하기
            if(AppState.ignoreIdle){
                return;
            }

            // 기존 타이머 취소 (디바운싱)
            clearTimeout(AppState.debounceTimer);

            // 0.5초 뒤 실행 예약
            AppState.debounceTimer = setTimeout(() => {
                const bounds = map.getBounds();

                // 🛑 무한 루프 방지 브레이크
                if (AppState.lastBounds && bounds.equals(AppState.lastBounds)) {
                    console.log("✋ 지도가 움직이지 않아 데이터 요청을 건너뜁니다.");
                    return;
                }

                // 범위가 달라졌을 때만 갱신하고 데이터를 요청함
                AppState.lastBounds = bounds;
                JobService.loadJobs(bounds);

            }, 500);
        });

        // 3. 지도 배경 클릭 시 카드 닫기
        map.addListener("click", () => {
            UIManager.closeJobCard();
        });
    },

    // 내 위치로 이동 (GPS)
    moveToCurrentLocation: function() {
        if (!navigator.geolocation) {
            alert("브라우저가 위치 정보를 지원하지 않습니다.");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const pos = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude,
                };

                AppState.map.setCenter(pos);
                AppState.map.setZoom(15);

                // 내 위치 파란 점 표시
                new google.maps.Marker({
                    position: pos,
                    map: AppState.map,
                    title: "내 위치",
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 10,
                        fillColor: "#4285F4",
                        fillOpacity: 1,
                        strokeWeight: 2,
                        strokeColor: "white",
                    },
                });

                // 🌟 [핵심 수정] 이동이 끝난 직후(idle) 즉시 데이터 로딩
                // 일반적인 idle 리스너는 0.5초 딜레이가 있지만, 여기서는 즉시 실행합니다.
                google.maps.event.addListenerOnce(AppState.map, 'idle', function() {

                    // 전역 idle 리스너에 의해 중복 실행되는 것을 방지하기 위해 타이머 취소
                    clearTimeout(AppState.debounceTimer);

                    // 즉시 로딩 실행
                    const bounds = AppState.map.getBounds();

                    // 🌟 [추가] 강제 로딩 시에도 현재 범위를 '마지막 범위'로 등록해둬야
                    // 이후에 자동 idle 이벤트가 중복 실행되는 것을 막을 수 있습니다.
                    AppState.lastBounds = bounds;

                    JobService.loadJobs(bounds);
                });
            },
            () => { alert("위치 정보를 가져올 수 없습니다."); }
        );
    },

    // 마스킹(배경 어둡게) 그리기
    drawMasking: function() {
        const worldCoords = [
            { lat: 85, lng: -180 }, { lat: 85, lng: 0 }, { lat: 85, lng: 180 },
            { lat: -85, lng: 180 }, { lat: -85, lng: 0 }, { lat: -85, lng: -180 },
            { lat: 85, lng: -180 }
        ];

        // GeoJSON 유틸함수 사용 (하단 정의)
        const tokyoPaths = typeof tokyoGeoJson !== 'undefined' ? Utils.getPathsFromGeoJson(tokyoGeoJson) : [];
        const osakaCityPaths = typeof osakaCityGeoJson !== 'undefined' ? Utils.getPathsFromGeoJson(osakaCityGeoJson) : [];
        const kansaiPaths = typeof osakaGeoJson !== 'undefined' ? Utils.getPathsFromGeoJson(osakaGeoJson, 1) : [];

        // 다크모드 감지 함수
        const isDark = document.body.classList.contains('dark-mode');
        const borderStyle = MapManager.getBoundaryStyle(isDark);

        AppState.maskPolygon = new google.maps.Polygon({
            paths: [worldCoords, ...tokyoPaths, ...osakaCityPaths, ...kansaiPaths],
            strokeColor: borderStyle.strokeColor,
            strokeOpacity: borderStyle.strokeOpacity,
            strokeWeight: borderStyle.strokeWeight,
            fillColor: "#000000",
            fillOpacity: 0.6,
            map: AppState.map,
            clickable: false
        });
    },


    // 🌟 [NEW] 테마 변경 실시간 감지 함수
    observeThemeChange: function() {
        // MutationObserver: HTML 요소의 변화를 감시하는 기능
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                // body 태그의 class 속성이 변했을 때만 실행
                if (mutation.attributeName === 'class') {
                    const isDarkMode = document.body.classList.contains('dark-mode');
                    MapManager.setMapStyle(isDarkMode);
                }
            });
        });

        // body 태그 감시 시작 (속성 변화 감지)
        observer.observe(document.body, { attributes: true });
    },

    // 🌟 [NEW] 지도 스타일 갈아끼우기 함수
    setMapStyle: function(isDark) {
        if (!AppState.map) return;

        const newStyle = isDark ? MapStyles.dark : MapStyles.light;

        // setOptions를 통해 실행 중에 스타일만 쏙 바꿉니다.
        AppState.map.setOptions({ styles: newStyle });

        console.log(`🎨 지도 테마 변경: ${isDark ? 'Dark' : 'Light'}`);

        if (AppState.maskPolygon) {
            AppState.maskPolygon.setMap(null);
        }

        MapManager.drawMasking();
    },

    // 🎨 [NEW] 모드에 따른 경계선 반환 함수
    getBoundaryStyle: function (isDark) {
        const boundaryColor = isDark ? '#FF6B6B' : '#fB0000';

        return {
            strokeColor : boundaryColor,
            strokeOpacity: 1.0,
            strokeWeight: 2
        }
    },


    // 🌟 [NEW] 마커들이 모두 보이게 지도 카메라 자동 조절
    // fitBoundsToData: function(jobs) {
    //     if (!jobs || jobs.length === 0 || !AppState.map) return;
    //
    //     // 1. 카메라가 비출 '영역(경계)' 객체 생성
    //     const bounds = new google.maps.LatLngBounds();
    //     let hasValidCoords = false;
    //
    //     // 2. 공고들의 좌표를 하나씩 영역에 추가 (영역이 점점 넓어짐)
    //     jobs.forEach(job => {
    //         if (job.lat && job.lng) {
    //             bounds.extend(new google.maps.LatLng(job.lat, job.lng));
    //             hasValidCoords = true;
    //         }
    //     });
    //
    //     // 3. 유효한 좌표가 있다면 지도를 해당 영역에 맞춤
    //     if (hasValidCoords) {
    //         AppState.ignoreIdle = true;
    //
    //         AppState.map.fitBounds(bounds);
    //
    //         // 2. 지도 이동이 완전히 끝났을 때(idle) 실행
    //         google.maps.event.addListenerOnce(AppState.map, "idle", function() {
    //             AppState.map.setZoom(20); // 최대 줌 레벨을 20로 제한
    //
    //             // 🌟 중요: 줌 조절까지 완전히 끝난 후에야 스위치를 끄고, 현재 영역을 저장함
    //             // setTimeout을 아주 짧게 줘서 마지막 줌 조절 idle 이벤트까지 무시하도록 안전장치
    //             setTimeout(() => {
    //                 AppState.lastBounds = AppState.map.getBounds();
    //                 AppState.ignoreIdle = false; // 이제부터 다시 자동 갱신
    //
    //                 // ========================================================
    //                 // 🌟 [NEW] 사용자 편의성 극대화 (UX 업데이트)
    //                 // ========================================================
    //
    //                 // 🌟 [핵심 변경] autoOpenCard가 true일 때만 첫 번째 카드를 자동으로 엽니다.
    //                 // 최근 본 공고 리스트(바텀 시트)를 볼 때는 카드가 열리지 않아 깔끔합니다.
    //                 if (autoOpenCard && jobs[0]) {
    //                     UIManager.openJobCard(jobs[0]);
    //                 }
    //
    //                 // 2) 화면에 있는 마커들을 위아래로 통통 튀게 만듭니다. (BOUNCE)
    //                 AppState.jobMarkers.forEach(marker => {
    //                     // 구글 맵 기본 제공 애니메이션 적용
    //                     marker.setAnimation(google.maps.Animation.BOUNCE);
    //
    //                     // 💡 UX 꿀팁: 계속 통통 튀면 눈이 피로할 수 있으니,
    //                     // 2.5초(2500ms) 뒤에 알아서 멈추도록 센스를 발휘합니다.
    //                     setTimeout(() => {
    //                         marker.setAnimation(null);
    //                     }, 2500);
    //                 });
    //
    //             }, 100);
    //         });
    //     }
    // },

    // 🌟 [NEW] 지역 변경 함수
    changeRegion: function(regionCode) {
        if (!AppState.map) return;

        // 1. 지도가 휙 이동하는 동안 쓸데없는 API 요청이 가지 않도록 스위치 ON
        AppState.ignoreIdle = true;

        // 2. 지역별 좌표 설정
        let targetPos;
        let targetZoom = 10; // 기본 줌 레벨

        if (regionCode === 'tokyo') {
            targetPos = { lat: 35.6895, lng: 139.6921 };
            targetZoom = 18;
        } else if (regionCode === 'osaka') {
            targetPos = { lat: 34.6938, lng: 135.5019 };
            targetZoom = 18; // 오사카는 11 정도가 보기 좋을 수 있습니다.
        }

        // 3. 지도 카메라 부드럽게 이동 (panTo)
        AppState.map.panTo(targetPos);
        AppState.map.setZoom(targetZoom);

        // 4. 이동이 완료된 후 새로운 지역의 데이터를 불러오도록 타이머 세팅
        setTimeout(() => {
            AppState.ignoreIdle = false; // 스위치 OFF (이제 다시 자동 갱신됨)

            // 현재 화면 범위 저장 및 데이터 요청
            const bounds = AppState.map.getBounds();
            AppState.lastBounds = bounds;
            JobService.loadJobs(bounds);

        }, 800); // 0.8초 후 (지도가 부드럽게 날아가는 시간 대기)
    },

    // 🌟 [NEW] 시트에서 리스트 클릭 시 해당 위치로 지도 슉~ 이동하기
    moveToJobLocation: function(lat, lng) {
        if (!AppState.map || !lat || !lng) return;

        // 1. 지도가 휙 이동하는 동안 새 데이터 불러오기(idle) 방지 스위치 ON!
        AppState.ignoreIdle = true;

        // 2. 해당 위치로 부드럽게 카메라 이동 및 줌인
        const targetPos = { lat: parseFloat(lat), lng: parseFloat(lng) };
        AppState.map.panTo(targetPos);
        AppState.map.setZoom(18); // 상세히 볼 수 있게 줌 레벨 조정

        // 3. 이동이 완전히 끝났을 때의 처리
        google.maps.event.addListenerOnce(AppState.map, "idle", function() {
            setTimeout(() => {
                // 현재 이동한 위치를 '마지막 위치'로 강제 저장해둬서
                // 스위치를 끈 직후에 데이터 갱신이 또 일어나는 것을 완벽 차단!
                AppState.lastBounds = AppState.map.getBounds();
                AppState.ignoreIdle = false; // 스위치 OFF (이제 다시 손으로 움직이면 갱신됨)

                // (보너스) 이동한 곳의 마커를 찾아서 통통 튀게(Bounce) 만들어주기!
                const targetMarker = AppState.jobMarkers.find(
                    m => m.getPosition().lat().toFixed(4) === targetPos.lat.toFixed(4) &&
                        m.getPosition().lng().toFixed(4) === targetPos.lng.toFixed(4)
                );

                if (targetMarker) {
                    targetMarker.setAnimation(google.maps.Animation.BOUNCE);
                    setTimeout(() => targetMarker.setAnimation(null), 2500); // 2.5초 뒤 멈춤
                }
            }, 100);
        });
    },
};

// ============================================================
// [4] 데이터 서비스 (Job Service - AJAX)
// ============================================================
const JobService = {
    loadJobs: function(bounds) {
        if (!AppState.map) return;

        // 🌟 삼항 연산자 대신 MapMessages 사용
        $('#listBody').html(`<tr><td colspan="7" class="msg-box">${MapMessages.loading}</td></tr>`);

        // 파라미터 준비
        const params = JobService.prepareParams(bounds);

        // 이전 요청 취소 (AbortController 대신 jQuery xhr.abort 사용)
        if (AppState.currentXhr && AppState.currentXhr.readyState !== 4) {
            AppState.currentXhr.abort();
        }

        // jQuery AJAX 요청
        AppState.currentXhr = $.ajax({
            url: '/map/api/jobs',
            method: 'GET',
            data: params,
            dataType: 'json',
            success: function(data) {
                JobService.processData(data);
            },
            error: function(xhr, status, error) {
                if (status !== 'abort') {
                    console.error("AJAX Error:", error);
                    $('#listBody').html(`<tr><td colspan="7" class="msg-box">${MapMessages.loadFail}</td></tr>`);
                }
            }
        });
    },

    prepareParams: function(bounds) {
        const params = {};
        if (bounds) {
            const ne = bounds.getNorthEast();
            const sw = bounds.getSouthWest();
            params.minLat = sw.lat();
            params.maxLat = ne.lat();
            params.minLng = sw.lng();
            params.maxLng = ne.lng();
        } else {
            const urlParams = new URLSearchParams(window.location.search);
            params.minLat = urlParams.get('minLat') || 0;
        }

        // 언어 설정
        const currentLang = new URLSearchParams(window.location.search).get('lang') === 'ja' ? 'ja' : 'kr';
        UIManager.updateTableHeader(currentLang);
        params.lang = currentLang;

        return params;
    },

    processData: function(data) {

        console.log(`출력 데이터: ${data.length}개`);

        // UI 업데이트
        MarkerManager.clearMarkers();
        UIManager.renderList(data);
        MarkerManager.renderMarkers(data);
    },

    // 🌟 [저장된 공고] DB에서 스크랩 내역 가져오기
    loadSavedJobs: function() {
        $.ajax({
            url: '/api/scraps',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                UIManager.renderList(data);
                MarkerManager.renderMarkers(data);

                // 👉 [추가] 마커를 다 찍었으면 그쪽으로 카메라 이동!
                // MapManager.fitBoundsToData(data);
            },
            error: function(err) {
                console.error("찜한 목록 불러오기 실패:", err);
                $('#listBody').html(`<tr><td colspan="7" class="msg-box">${MapMessages.savedFail}</td></tr>`);
            }
        });
    },

    // 🌟 [최근 본 공고] 브라우저 로컬 스토리지에서 가져오기
    loadRecentJobs: function() {
        const recentJobsJson = localStorage.getItem('kumo_recent_jobs');
        let recentJobs = [];

        if (recentJobsJson) {
            recentJobs = JSON.parse(recentJobsJson);
        }

        // ========================================================
        // 🌟 [수정 완료] 1개만 제한하던 코드를 지우고, 배열 전체(최대 20개)를 넘겨줍니다!
        // ========================================================
        UIManager.renderList(recentJobs);
        MarkerManager.renderMarkers(recentJobs);

        // 👉 바텀 시트를 위로 스르륵 올립니다.
        $('#bottomSheet').addClass('active');

        // 👉 바텀 시트가 올라올 때, 기존에 떠있던 카드가 있다면 가려지지 않게 닫아줍니다.
        UIManager.closeJobCard();

        // 👉 지도 카메라를 최근 본 공고들이 모두 화면에 들어오도록 조절합니다.
        // (두 번째 인자로 false를 넘겨서 카드가 자동으로 열리지 않도록 막습니다)
        // MapManager.fitBoundsToData(recentJobs, false);
    },

    addRecentJob: function(jobData) {
        if (!jobData || !jobData.id) return;

        // 1. 기존 데이터 꺼내오기 (없으면 빈 배열)
        const recentStr = localStorage.getItem('kumo_recent_jobs');
        let recentJobs = recentStr ? JSON.parse(recentStr) : [];

        // 2. 중복 제거 (이미 본 공고를 또 눌렀다면, 예전 기록을 지우고 최신으로 올리기 위해)
        recentJobs = recentJobs.filter(job => job.id !== jobData.id);

        // 3. 배열의 맨 앞(최신)에 추가
        recentJobs.unshift(jobData);

        // 4. 최대 20개까지만 유지 (용량 낭비 방지)
        if (recentJobs.length > 20) {
            recentJobs = recentJobs.slice(0, 20); // 20개까지만 자르기
        }

        // 5. 다시 문자열로 바꿔서 로컬스토리지에 저장
        localStorage.setItem('kumo_recent_jobs', JSON.stringify(recentJobs));

        console.log(`💾 최근 본 공고 저장됨 (총 ${recentJobs.length}개)`);
    }
};

// ============================================================
// [5] 마커 관리자 (Marker Manager - Clustering)
// ============================================================
const MarkerManager = {
    renderMarkers: function(jobs) {
        if (!jobs || jobs.length === 0) return;

        const map = AppState.map;
        AppState.jobMarkers = []; // 초기화

        // 마커 생성
        const markers = jobs
            .filter(job => job.lat && job.lng)
            .map(job => {
                const marker = new google.maps.Marker({
                    position: { lat: job.lat, lng: job.lng },
                    title: job.title,
                });

                marker.addListener("click", () => {
                    UIManager.openJobCard(job);
                });

                return marker;
            });

        AppState.jobMarkers = markers;

        // 클러스터러 업데이트
        if (AppState.markerCluster) {
            AppState.markerCluster.clearMarkers();
            AppState.markerCluster.addMarkers(markers);
        } else {
            AppState.markerCluster = new markerClusterer.MarkerClusterer({
                map,
                markers,
                renderer: MarkerManager.getClusterRenderer(), // 커스텀 스타일
                algorithm: new markerClusterer.GridAlgorithm({
                    gridSize: 80, // 구 단위 느낌
                    maxZoom: 15
                })
            });
        }
    },

    clearMarkers: function() {
        if (AppState.markerCluster) {
            AppState.markerCluster.clearMarkers();
        }
        AppState.jobMarkers = [];
    },

    // 클러스터 스타일 정의 (파란색 큰 원)
    getClusterRenderer: function() {
        return {
            render: ({ count, position }) => {
                return new google.maps.Marker({
                    label: { text: String(count), color: "white", fontSize: "14px", fontWeight: "bold" },
                    position,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 25,
                        fillColor: "#4285F4",
                        fillOpacity: 0.9,
                        strokeWeight: 4,
                        strokeColor: "rgba(255, 255, 255, 0.5)"
                    },
                    zIndex: Number(google.maps.Marker.MAX_ZINDEX) + count,
                });
            }
        };
    }
};

// ============================================================
// [6] UI 관리자 (UI Manager - jQuery)
const UIManager = {
    // 🔄 [NEW] 하단 탭 전환 함수
    // 🔄 [Refactored] 탭 기능 분기 처리
    // 🔄 [Refactored] 탭 기능 분기 처리
    switchTab: function(tabName) {
        console.log(`탭 전환 기능 실행: ${tabName}`);

        // (UI 변경 코드는 위쪽 이벤트 리스너로 이사 갔음! 삭제됨)

        // 기능별 로직만 남음
        if (tabName === 'nearby') {
            AppState.isLocationMode = true;
            MapManager.moveToCurrentLocation();
        }
        else if (tabName === 'saved') {
            // TODO: 저장된 공고 불러오기
            JobService.loadSavedJobs();
        }
        else if (tabName === 'recent') {
            // TODO: 최근 본 공고 불러오기
            JobService.loadRecentJobs();
        }
        else if (tabName === 'chat') {
            location.href = '/chat/room';
        }
    },

    // 🌟 [핵심] job_list.html의 로직을 여기로 통합!
    renderList: function(jobs) {
        const $tbody = $('#listBody');
        const currentLang = new URLSearchParams(window.location.search).get('lang') === 'ja' ? 'ja' : 'kr';

        if (!jobs || jobs.length === 0) {
            $tbody.html(`<tr><td colspan="7" class="msg-box">${MapMessages.emptyJob}</td></tr>`);
            return;
        }

        let html = '';
        jobs.forEach(job => {
            const title = job.title || MapMessages.fbTitle;
            const company = job.companyName || MapMessages.fbCompany;
            const wage = job.wage || MapMessages.fbWage;
            const address = job.address || '-';
            const thumb = job.thumbnailUrl || 'https://placehold.co/40';
            const dateStr = job.writeTime || MapMessages.fbTime;
            const contact = job.contactPhone || '-';

            const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${currentLang}`;

            const clickAttr = (job.lat && job.lng)
                ? `onclick="MapManager.moveToJobLocation(${job.lat}, ${job.lng})"`
                : `onclick="alert('지도 좌표 정보가 없습니다.')"`;

            // ========================================================
            // 🌟 [핵심 변경] 로그인 여부에 따라 찜 버튼 HTML을 다르게 생성합니다.
            // ========================================================
            const saveBtnHtml = isUserLoggedIn
                ? `<button class="btn">${MapMessages.btnSave}</button>`
                : ''; // 로그인 안 했으면 빈 문자열(버튼 없음)

            html += `
            <tr>
                <td>
                    <span class="title-text" style="cursor: pointer; text-decoration: underline; color: var(--text-main);" ${clickAttr}>
                        ${title}
                    </span>
                    <span class="badge bg-blue">${MapMessages.badgeRecruit}</span>
                    <span class="badge bg-yellow">${MapMessages.badgeUrgent}</span>
                </td>
                <td><a href="#" class="company-text">${company}</a></td>
                <td><span class="addr-text">${address}</span></td>
                <td><span class="wage-text">${wage}</span></td>
                <td><span class="contact-text">${contact}</span></td>
                <td>
                    <div class="profile-wrap">
                        <img src="${thumb}" class="profile-img" onerror="this.src='https://placehold.co/40?text=No+Img'">
                        <div class="profile-info">
                            <div>Admin</div>
                            <div>${dateStr}</div>
                        </div>
                    </div>
                </td>
                <td>
                     <div class="btn-wrap">
                        ${saveBtnHtml}
                        <button class="btn btn-view" onclick="location.href='${detailUrl}'">
                            ${MapMessages.btnDetail}
                        </button>
                     </div>
                </td>
            </tr>`;
        });

        $tbody.html(html);
        UIManager.updateTableHeader();
    },

    openJobCard: function(job) {
        const currentLang = new URLSearchParams(window.location.search).get('lang') === 'ja' ? 'ja' : 'kr';
        const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${currentLang}`;
        const $card = $('#jobDetailCard');

        // 🌟 삼항 연산자 싹 지우고 MapMessages 적용!
        $('#card-company').text(job.companyName || MapMessages.fbCompany);
        $('#card-manager').text(job.manager || MapMessages.fbManager);
        $('#card-title').text(job.title);

        $('.job-address').html(`${MapMessages.labelAddress} <span id="card-address">${job.address || '-'}</span>`);
        $('#card-phone').text(job.contactPhone || '-');

        $('#jobDetailCard .btn-outline').text(MapMessages.btnSaveCard);
        $('#btn-detail').text(MapMessages.btnDetailCard);

        const $img = $('#card-img');
        $img.attr('src', job.thumbnailUrl || 'https://placehold.co/300');
        $img.off('error').on('error', function() { $(this).attr('src', 'https://placehold.co/300?text=No+Image'); });

        $('#btn-detail').off('click').on('click', function() {
            window.location.href = detailUrl;
        });

        $card.show();
        $('#bottomSheet').removeClass('active');

        JobService.addRecentJob(job);
    },

    closeJobCard: function() {
        $('#jobDetailCard').hide();
    },

    // 🌟 테이블 헤더 언어 변경 함수도 엄청나게 짧아집니다!
    updateTableHeader: function() {
        const headers = $('#tableHeader th');
        // HTML에서 선언한 MapMessages.table 배열을 그대로 입혀줍니다.
        headers.each(function(index) {
            if(MapMessages.table[index]) $(this).text(MapMessages.table[index]);
        });
    }
};

// ============================================================
// [7] 유틸리티 (Utils)
// ============================================================
const Utils = {
    // GeoJSON -> Google Maps Paths
    getPathsFromGeoJson: function(json, specificIndex = -1) {
        const paths = [];
        if (!json) return paths;
        const features = (json.type === "FeatureCollection") ? json.features : [json];

        features.forEach(f => {
            if (!f.geometry) return;
            if (f.geometry.type === "MultiPolygon") {
                f.geometry.coordinates.forEach((polygon, index) => {
                    if (specificIndex >= 0 && index !== specificIndex) return;
                    paths.push(polygon[0].map(c => ({ lat: c[1], lng: c[0] })));
                });
            } else if (f.geometry.type === "Polygon") {
                paths.push(f.geometry.coordinates[0].map(c => ({ lat: c[1], lng: c[0] })));
            }
        });
        return paths;
    },

    // 거리 계산 (km)
    getDistanceFromLatLonInKm: function(lat1, lon1, lat2, lon2) {
        const R = 6371;
        const dLat = Utils.deg2rad(lat2 - lat1);
        const dLon = Utils.deg2rad(lon2 - lon1);
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Utils.deg2rad(lat1)) * Math.cos(Utils.deg2rad(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    },

    deg2rad: function(deg) {
        return deg * (Math.PI / 180);
    }
};