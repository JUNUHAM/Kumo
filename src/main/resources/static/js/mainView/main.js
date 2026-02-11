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
    lastBounds: null
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

    // 내 위치 찾기 이벤트
    $("#btn-my-location").on('click', function () {
        MapManager.moveToCurrentLocation();
    })

    $(".btn-close-card").on('click', function () {
        UIManager.closeJobCard();
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
            gestureHandling: 'greedy'
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
                google.maps.event.addListenerOnce(map, 'idle', function() {

                    // 전역 idle 리스너에 의해 중복 실행되는 것을 방지하기 위해 타이머 취소
                    clearTimeout(AppState.debounceTimer);

                    // 즉시 로딩 실행
                    const bounds = map.getBounds();

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

        new google.maps.Polygon({
            paths: [worldCoords, ...tokyoPaths, ...osakaCityPaths, ...kansaiPaths],
            strokeColor: "#FF0000", strokeOpacity: 0, strokeWeight: 0,
            fillColor: "#000000", fillOpacity: 0.6,
            map: AppState.map, clickable: false
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
    },
};

// ============================================================
// [4] 데이터 서비스 (Job Service - AJAX)
// ============================================================
const JobService = {
    loadJobs: function(bounds) {
        if (!AppState.map) return;

        $('#listBody').html('<tr><td colspan="7" class="msg-box">데이터 로딩 중...</td></tr>');

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
                    $('#listBody').html('<tr><td colspan="7" class="msg-box">데이터 로딩 실패</td></tr>');
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
    // 🌟 [핵심] job_list.html의 로직을 여기로 통합!
    renderList: function(jobs) {
        const $tbody = $('#listBody');
        // URL에서 lang 파라미터 가져오기 (없으면 'kr')
        const urlParams = new URLSearchParams(window.location.search);
        const lang = urlParams.get('lang') === 'ja' ? 'ja' : 'kr'; // ja 대신 jp로 통일 권장

        // 1. 데이터가 없을 때 처리
        if (!jobs || jobs.length === 0) {
            const emptyMsg = lang === 'ja' ? '現在、この地域には求人がありません。' : '현재 이 지역에 공고가 없습니다.';
            $tbody.html(`<tr><td colspan="7" class="msg-box">${emptyMsg}</td></tr>`);
            return;
        }

        let html = '';
        jobs.forEach(job => {
            // DTO에서 이미 언어 처리가 끝난 상태로 옴 (title, companyName 등)
            const title = job.title || (lang === 'ja' ? 'タイトルなし' : '제목 없음');
            const company = job.companyName || (lang === 'ja' ? '会社名未定' : '회사명 미정');
            const wage = job.wage || (lang === 'ja' ? '協議' : '협의');
            const address = job.address || '-';

            // 썸네일 & 날짜 등
            const thumb = job.thumbnailUrl || 'https://placehold.co/40';
            const dateStr = job.writeTime || (lang === 'ja' ? 'ついさっき' : '방금 전');
            const contact = job.contactPhone || '-';

            // 상세 페이지 URL
            const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}`;

            // 뱃지 텍스트
            const badgeRecruit = lang === 'ja' ? '募集中' : '구인중';
            const badgeUrgent = lang === 'ja' ? '急募' : '급구';
            const btnSave = lang === 'ja' ? '保存' : '찜';
            const btnDetail = lang === 'ja' ? '詳細' : '상세';

            html += `
            <tr>
                <td>
                    <span class="title-text">${title}</span>
                    <span class="badge bg-blue">${badgeRecruit}</span>
                    <span class="badge bg-yellow">${badgeUrgent}</span>
                </td>
                <td><a href="#" class="company-text">${company}</a></td>
                <td><span class="addr-text">${address}</span></td>
                <td><span class="wage-text">${wage}</span></td>
                <td style="color:#666; font-size:12px;">${contact}</td>
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
                        <button class="btn">${btnSave}</button>
                        <button class="btn btn-view" onclick="location.href='${detailUrl}'">
                            ${btnDetail}
                        </button>
                     </div>
                </td>
            </tr>`;
        });

        $tbody.html(html);

        // 🌟 테이블 헤더도 언어에 맞게 변경
        UIManager.updateTableHeader(lang);
    },

    openJobCard: function(job) {
        // ... (기존 openJobCard 코드 유지) ...
        // 단, 여기도 lang 체크해서 버튼 텍스트 등을 바꿔주면 좋습니다.
        const lang = new URLSearchParams(window.location.search).get('lang') || 'kr';
        // ...

        // (기존 코드 그대로 두셔도 무방합니다)
        const $card = $('#jobDetailCard');
        const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}`;

        $('#card-company').text(job.companyName || '회사명 미정');
        $('#card-manager').text(job.manager || '담당자');
        $('#card-title').text(job.title);
        $('#card-address').text(job.address);
        $('#card-phone').text(job.contactPhone || '-');

        const $img = $('#card-img');
        $img.attr('src', job.thumbnailUrl || 'https://placehold.co/300');
        $img.off('error').on('error', function() { $(this).attr('src', 'https://placehold.co/300?text=No+Image'); });

        $('#btn-detail').off('click').on('click', function() {
            window.location.href = detailUrl;
        });

        $card.show();
        $('#bottomSheet').removeClass('active');
    },

    closeJobCard: function() {
        $('#jobDetailCard').hide();
    },

    // 테이블 헤더 언어 변경 함수
    updateTableHeader: function(lang) {
        if (lang === 'ja') {
            const headers = $('#tableHeader th');
            const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];

            // jQuery each를 써서 안전하게 변경
            headers.each(function(index) {
                if(jpHeaders[index]) $(this).text(jpHeaders[index]);
            });
        } else {
            // 한국어 (기본값) 복구
            const headers = $('#tableHeader th');
            const krHeaders = ['제목', '상호명', '근무지', '급여', '연락처', '담당자', '관리'];
            headers.each(function(index) {
                if(krHeaders[index]) $(this).text(krHeaders[index]);
            });
        }
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