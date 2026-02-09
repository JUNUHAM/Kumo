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
    currentXhr: null          // 현재 진행 중인 AJAX 요청 (취소용)
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
});

// ============================================================
// [3] 지도 관리자 (Map Manager)
// ============================================================
const MapManager = {
    init: function() {
        const mapElement = document.getElementById('map');
        if (!mapElement) return;

        const tokyo = { lat: 35.6804, lng: 139.7690 };

        AppState.map = new google.maps.Map(mapElement, {
            center: tokyo,
            zoom: 10,
            disableDefaultUI: true,
            styles: [{ "stylers": [{ "saturation": -20 }] }]
        });

        MapManager.drawMasking();
        MapManager.bindMapEvents();
    },

    bindMapEvents: function() {
        const map = AppState.map;

        // 1. Idle 이벤트 (디바운싱 적용)
        map.addListener("idle", () => {
            clearTimeout(AppState.debounceTimer);
            AppState.debounceTimer = setTimeout(() => {
                const bounds = map.getBounds();
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
    }
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
        const currentLang = new URLSearchParams(window.location.search).get('lang') === 'jp' ? 'jp' : 'kr';
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
// ============================================================
const UIManager = {
    renderList: function(jobs) {
        const $tbody = $('#listBody');
        const lang = new URLSearchParams(window.location.search).get('lang') || 'kr';

        if (!jobs || jobs.length === 0) {
            $tbody.html(`<tr><td colspan="7" class="msg-box">조건에 맞는 공고가 없습니다.</td></tr>`);
            return;
        }

        let html = '';
        jobs.forEach(job => {
            const title = (lang === 'jp' && job.titleJp) ? job.titleJp : job.title;
            const company = (lang === 'jp' && job.companyNameJp) ? job.companyNameJp : job.companyName;
            const wage = (lang === 'jp' && job.wageJp) ? job.wageJp : (job.wage || '협의');
            const address = job.address || '-';
            const thumb = job.thumbnailUrl || 'https://via.placeholder.com/40';
            const dateStr = job.writeTime || 'Recently';
            const contact = job.contactPhone || '-';

            // 상세 페이지 URL 생성
            const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}`;

            html += `
            <tr>
                <td>
                    <span class="title-text">${title}</span>
                    <span class="badge bg-blue">${lang === 'jp' ? '募集中' : '구인중'}</span>
                </td>
                <td><a href="#" class="company-text">${company}</a></td>
                <td><span class="addr-text">${address}</span></td>
                <td><span class="wage-text">${wage}</span></td>
                <td style="color:#666; font-size:12px;">${contact}</td>
                <td>
                    <div class="profile-wrap">
                        <img src="${thumb}" class="profile-img" onerror="this.src='https://via.placeholder.com/40'">
                        <div class="profile-info"><div>Admin</div><div>${dateStr}</div></div>
                    </div>
                </td>
                <td>
                     <div class="btn-wrap">
                        <button class="btn btn-view" onclick="location.href='${detailUrl}'">
                            ${lang === 'jp' ? '詳細' : '상세'}
                        </button>
                     </div>
                </td>
            </tr>`;
        });

        $tbody.html(html);
    },

    openJobCard: function(job) {
        const $card = $('#jobDetailCard');
        const lang = new URLSearchParams(window.location.search).get('lang') || 'kr';
        const detailUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}`;

        // 데이터 채우기 (jQuery 사용)
        $('#card-company').text(job.companyName || '회사명 미정');
        $('#card-manager').text(job.manager || '담당자');
        $('#card-title').text(job.title);
        $('#card-address').text(job.address);
        $('#card-phone').text(job.contactPhone || '-');

        const $img = $('#card-img');
        $img.attr('src', job.thumbnailUrl || 'https://via.placeholder.com/300');
        $img.on('error', function() { $(this).attr('src', 'https://via.placeholder.com/300?text=No+Image'); });

        // 버튼 이벤트
        $('#btn-detail').off('click').on('click', function() {
            window.location.href = detailUrl;
        });

        $card.show();
        $('#bottomSheet').removeClass('active');
    },

    closeJobCard: function() {
        $('#jobDetailCard').hide();
    },

    updateTableHeader: function(lang) {
        if (lang === 'jp') {
            const headers = $('#tableHeader th');
            const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];
            headers.each(function(index) {
                if(jpHeaders[index]) $(this).text(jpHeaders[index]);
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

/**
 *      이하 레거시 코드
 * */


// // 지도 생성
// let map;
// let jobMarkers = []; // 직업 마커 보관 배열
// let markerCluster = null;
//
// // 🌟 [추가] 자원 관리용 변수
// let debounceTimer;        // 딜레이 타이머
// let abortController = null; // 네트워크 요청 취소용 컨트롤러
// let radiusCircle = null; // 🌟 [추가] 반경 500m 원을 저장할 변수
// let isLocationMode = false; // 🌟 [NEW] 내 주변 보기 모드 스위치 (기본값: 꺼짐)
//
// // Jquery를 사용하면 오히려 번거롭기 때문에 Vanilla JS를 사용하여 map 구축
// function initMap() {
//     // [수정 후] 컨테이너 안에 있는 id="map" 요소를 찾음
//     let mapElement = document.getElementById('map');
//
//     if (!mapElement) return;
//
//     const tokyo = { lat: 35.6804, lng: 139.7690 };
//
//     map = new google.maps.Map(mapElement, {
//         center: tokyo,
//         zoom: 10,
//         disableDefaultUI: true,
//         style: [
//             {"stylers" : [{"saturation":-20}]},
//         ]
//     });
//
//     drawMasking();
//
//     // 🌟 [수정] 이벤트 리스너: 디바운싱 적용
//     map.addListener("idle", () => {
//         // 1. 기존에 대기 중이던 타이머가 있다면 취소 (아직 유저가 지도 조작 중이라는 뜻)
//         clearTimeout(debounceTimer);
//
//         // 2. 0.5초(500ms) 뒤에 실행하도록 예약
//         debounceTimer = setTimeout(() => {
//             const bounds = map.getBounds();
//             loadJobs(bounds);
//         }, 500);
//     });
//
//     // 🌟 [NEW] 사용자가 지도를 드래그하면 '내 주변 보기' 모드 해제!
//     map.addListener("dragstart", () => {
//         if (isLocationMode) {
//             console.log("사용자가 지도를 움직여 내 주변 모드를 해제합니다.");
//             isLocationMode = false;
//             // (선택) 움직이자마자 원을 지우고 싶다면 아래 주석 해제
//             if (radiusCircle) radiusCircle.setMap(null);
//         }
//     });
//
//     map.addListener("click", () => {
//         closeJobCard();
//     });
// }
//
// // HTML 문서가 다 로딩되면 실행 (jQuery의 ready 함수) <- Jquery 사용
// $(function() {
//     // 1. '.sheet-handle' 클래스를 가진 요소를 클릭하면
//     $('.sheet-handle').on('click', function() {
//         // 2. '#bottomSheet' 아이디를 가진 요소에 'active' 클래스를 줬다 뺏었다 함
//         $('#bottomSheet').toggleClass('active');
//
//         // 3. 만약 bottomSheet가 올라오면 플로팅 카드를 제거해주세용
//         if($("#bottomSheet").hasClass('active')) {
//             // 리스트 올라올시 카드 제거
//             closeJobCard();
//         }
//     });
// });
//
// // 전역 등록
// window.initMap = initMap;
//
// function drawMasking() {
//     // 1. 전세계를 덮는 거대한 회색 사각형 좌표 (외각선)
//     const worldCoords = [
//         { lat: 85, lng: -180 }, { lat: 85, lng: 0 }, { lat: 85, lng: 180 },
//         { lat: -85, lng: 180 }, { lat: -85, lng: 0 }, { lat: -85, lng: -180 },
//         { lat: 85, lng: -180 }
//     ];
//
//     // 2. HTML에서 로드한 GeoJSON 파일 변수가 있는지 확인하고 경로 추출
//     // (파일이 없어도 에러 안 나게 빈 배열 처리)
//     const tokyoPaths = typeof tokyoGeoJson !== 'undefined' ? getPathsFromGeoJson(tokyoGeoJson) : [];
//     const osakaCityPaths = typeof osakaCityGeoJson !== 'undefined' ? getPathsFromGeoJson(osakaCityGeoJson) : [];
//     const kansaiPaths = typeof osakaGeoJson !== 'undefined' ? getPathsFromGeoJson(osakaGeoJson, 1) : [];
//
//     // 3. 폴리곤 그리기
//     // paths의 첫 번째 배열은 '색칠할 영역(전세계)', 그 뒤의 배열들은 '구멍 뚫을 영역(도쿄,오사카)'이 됩니다.
//     new google.maps.Polygon({
//         paths: [worldCoords, ...tokyoPaths, ...osakaCityPaths, ...kansaiPaths],
//         strokeColor: "#FF0000", // 경계선 색 (필요 없으면 투명하게)
//         strokeOpacity: 0,
//         strokeWeight: 0,
//         fillColor: "#000000",   // 배경 색 (검정)
//         fillOpacity: 0.6,       // 투명도 (0.6 정도가 적당)
//         map: map,
//         clickable: false        // 배경 클릭 안 되게
//     });
// }
//
// // GeoJSON 데이터를 구글 맵 Path로 변환하는 헬퍼 함수
// function getPathsFromGeoJson(json, specificIndex = -1) {
//     const paths = [];
//     if (!json) return paths;
//
//     // FeatureCollection인지 단일 Feature인지 확인
//     const features = (json.type === "FeatureCollection") ? json.features : [json];
//
//     features.forEach(f => {
//         if (!f.geometry) return;
//
//         if (f.geometry.type === "MultiPolygon") {
//             f.geometry.coordinates.forEach((polygon, index) => {
//                 if (specificIndex >= 0 && index !== specificIndex) return;
//                 // 구글 맵은 [Lng, Lat] 순서인 GeoJSON을 [Lat, Lng] 객체로 변환해야 함
//                 paths.push(polygon[0].map(c => ({ lat: c[1], lng: c[0] })));
//             });
//         } else if (f.geometry.type === "Polygon") {
//             paths.push(f.geometry.coordinates[0].map(c => ({ lat: c[1], lng: c[0] })));
//         }
//     });
//     return paths;
// }
//
// // [추가] 공고 리스트 불러오기 함수
// function loadJobs(bounds) {
//     const listBody = document.getElementById('listBody');
//     listBody.innerHTML = '<tr><td colspan="7" class="msg-box">데이터 로딩 중...</td></tr>';
//
//     // 1. 현재 지도의 좌표 범위(bounds) 가져오기 (없으면 URL 파라미터 사용)
//     const params = new URLSearchParams();
//
//     if (bounds) {
//         // 구글 맵에서 넘어온 좌표 범위
//         const ne = bounds.getNorthEast();
//         const sw = bounds.getSouthWest();
//         params.append('minLat', sw.lat());
//         params.append('maxLat', ne.lat());
//         params.append('minLng', sw.lng());
//         params.append('maxLng', ne.lng());
//     } else {
//         // 초기 로딩 시 (URL 파라미터 등)
//         const urlParams = new URLSearchParams(window.location.search);
//         params.append('minLat', urlParams.get('minLat') || 0);
//         // ... 필요한 값 처리
//     }
//
//     // 언어 설정 확인
//     const currentLang = new URLSearchParams(window.location.search).get('lang') === 'jp' ? 'jp' : 'kr';
//     updateTableHeader(currentLang); // 헤더 언어 변경
//
//     // 🌟 [추가] 이전 네트워크 요청이 아직 살아있다면 강제 취소!
//     if (abortController) {
//         abortController.abort();
//     }
//     // 새 컨트롤러 생성
//     abortController = new AbortController();
//     const signal = abortController.signal;
//
//     // 2. API 호출
//     fetch(`/map/api/jobs?${params.toString()}`,{ signal: signal })
//         .then(res => res.json())
//         .then(data => {
//             if (!map) return;
//
//             // 최종적으로 그려질 데이터 (일단 전체 데이터로 시작)
//             let finalData = data;
//
//             // 🌟 [핵심 로직] 모드에 따라 다르게 동작
//             if (isLocationMode) {
//                 // [CASE A] 내 주변 보기 모드 (ON)
//                 console.log("📍 내 주변 500m 필터링 적용 중...");
//
//                 const center = map.getCenter();
//                 drawRadiusCircle(center); // 파란 원 그리기
//
//                 // 500m 필터링 수행
//                 finalData = data.filter(job => {
//                     if (!job.lat || !job.lng) return false;
//                     const dist = getDistanceFromLatLonInKm(
//                         center.lat(), center.lng(),
//                         job.lat, job.lng
//                     );
//                     return dist <= 0.5;
//                 });
//
//             } else {
//                 // [CASE B] 일반 모드 (OFF)
//                 // 원이 그려져 있다면 지운다
//                 if (radiusCircle) {
//                     radiusCircle.setMap(null);
//                 }
//                 // finalData는 이미 전체 데이터(data)이므로 필터링 안 함
//             }
//
//             console.log(`모드: ${isLocationMode ? '내 주변' : '일반'}, 출력 개수: ${finalData.length}`);
//
//             clearMarkers();
//             renderList(finalData, new URLSearchParams(window.location.search).get('lang') || 'kr');
//             renderMarkers(finalData);
//         })
//         .catch(err => {
//             if (err.name === 'AbortError') {
//                 console.log('이전 요청 취소됨 (정상)'); // 에러 아님
//             } else {
//                 console.error(err);
//                 listBody.innerHTML = `<tr><td colspan="7" class="msg-box">데이터 로딩 실패</td></tr>`;
//             }
//         });
// }
//
// // [추가] 리스트 렌더링 (작성하신 코드 그대로 사용)
// function renderList(jobs, lang) {
//     const tbody = document.getElementById('listBody');
//     if (!jobs || jobs.length === 0) {
//         tbody.innerHTML = `<tr><td colspan="7" class="msg-box">조건에 맞는 공고가 없습니다.</td></tr>`;
//         return;
//     }
//
//     let html = '';
//     jobs.forEach(job => {
//         // ... (작성하신 데이터 매핑 로직 그대로 복사) ...
//         const title = (lang === 'jp' && job.titleJp) ? job.titleJp : job.title;
//         const company = (lang === 'jp' && job.companyNameJp) ? job.companyNameJp : job.companyName;
//         const wage = (lang === 'jp' && job.wageJp) ? job.wageJp : (job.wage || '협의');
//         const address = job.address || '-';
//         const thumb = job.thumbnailUrl || 'https://via.placeholder.com/40'; // 이미지 없으면 기본값
//         const dateStr = job.writeTime || 'Recently';
//         const contact = job.contactPhone || '-';
//
//         html += `
//         <tr>
//             <td>
//                 <span class="title-text">${title}</span>
//                 <span class="badge bg-blue">${lang === 'jp' ? '募集中' : '구인중'}</span>
//             </td>
//             <td><a href="#" class="company-text">${company}</a></td>
//             <td><span class="addr-text">${address}</span></td>
//             <td><span class="wage-text">${wage}</span></td>
//             <td style="color:#666; font-size:12px;">${contact}</td>
//             <td>
//                 <div class="profile-wrap">
//                     <img src="${thumb}" class="profile-img"
//                         onerror="this.onerror=null; this.src='https://via.placeholder.com/40?text=No+Img';">
//                     <div class="profile-info"><div>Admin</div><div>${dateStr}</div></div>
//                 </div>
//             </td>
//             <td>
//                  <div class="btn-wrap">
//                     <button class="btn btn-view"
//                         onclick="location.href='/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${lang}'">
//                         ${lang === 'jp' ? '詳細' : '상세'}
//                     </button>
//                  </div>
//             </td>
//         </tr>
//         `;
//     });
//
//     tbody.innerHTML = html;
// }
//
// // 🌟 [수정] 마커 렌더링 (커스텀 클러스터 + 구 단위 그리드)
// function renderMarkers(jobs) {
//     if (!jobs || jobs.length === 0) return;
//
//     // 1. 기존 마커 초기화
//     jobMarkers = [];
//
//     // 2. 마커 객체 생성
//     const markers = jobs
//         .filter(job => job.lat && job.lng)
//         .map(job => {
//             const marker = new google.maps.Marker({
//                 position: { lat: job.lat, lng: job.lng },
//                 title: job.title,
//             });
//
//             marker.addListener("click", () => {
//                 openJobCard(job);
//             });
//
//             return marker;
//         });
//
//     jobMarkers = markers;
//
//     // 3. 클러스터러 생성 또는 업데이트
//     if (markerCluster) {
//         markerCluster.clearMarkers();
//         markerCluster.addMarkers(markers);
//     } else {
//         // 🌟 [핵심] 커스텀 렌더러 (원의 크기와 색상을 결정)
//         const renderer = {
//             render: ({ count, position }) => {
//                 // 개수에 따라 원의 색상을 다르게 할 수도 있습니다.
//                 // 여기서는 통일된 파란색 큰 원으로 설정합니다.
//
//                 return new google.maps.Marker({
//                     label: {
//                         text: String(count),
//                         color: "white",
//                         fontSize: "14px",
//                         fontWeight: "bold"
//                     },
//                     position,
//                     // SVG 아이콘을 사용하여 원을 직접 그립니다.
//                     icon: {
//                         path: google.maps.SymbolPath.CIRCLE,
//                         scale: 25, // 🔴 원의 크기 (기본값보다 훨씬 크게 설정)
//                         fillColor: "#4285F4", // 내부 색상 (구글 파란색)
//                         fillOpacity: 0.9,
//                         strokeWeight: 4,      // 테두리 두께
//                         strokeColor: "rgba(255, 255, 255, 0.5)" // 테두리 색상 (반투명 흰색)
//                     },
//                     // 클러스터가 마커보다 위에 오도록 zIndex 설정
//                     zIndex: Number(google.maps.Marker.MAX_ZINDEX) + count,
//                 });
//             }
//         };
//
//         markerCluster = new markerClusterer.MarkerClusterer({
//             map,
//             markers,
//             renderer: renderer, // 위에서 만든 커스텀 스타일 적용
//
//             // 🌟 [핵심] 알고리즘 설정 (구 단위 묶기)
//             algorithm: new markerClusterer.GridAlgorithm({
//                 gridSize: 80, // 🔴 이 값을 키울수록 더 넓은 지역(구 단위)을 하나로 묶습니다. (기본값: 60)
//                 maxZoom: 15   // 13레벨(동네 수준)까지는 묶여있고, 더 확대하면 풀립니다.
//             })
//         });
//     }
// }
//
// // 🌟 [수정] 마커 및 클러스터 삭제
// function clearMarkers() {
//     // 1. 클러스터러가 관리하는 마커들 제거
//     if (markerCluster) {
//         markerCluster.clearMarkers();
//     }
//
//     // 2. 혹시 몰라 배열도 비움
//     jobMarkers = [];
// }
//
// // [추가] 헤더 언어 변경
// function updateTableHeader(lang) {
//     if (lang === 'jp') {
//         const headers = document.querySelectorAll('#tableHeader th');
//         const jpHeaders = ['タイトル', '会社名', '勤務地', '給与', '連絡先', '担当者', '管理'];
//         headers.forEach((th, idx) => { if(jpHeaders[idx]) th.innerText = jpHeaders[idx]; });
//     }
// }
//
// // 🌟 [NEW] 카드 열기 함수 & 자세히 보기 이벤트 연결
// function openJobCard(job) {
//     const card = document.getElementById('jobDetailCard');
//
//     // 1. 데이터 채워넣기 (기존 코드 유지)
//     document.getElementById('card-company').innerText = job.companyName || '회사명 미정';
//     document.getElementById('card-manager').innerText = job.manager || '담당자';
//
//     // 이미지 에러 처리 포함
//     const imgEl = document.getElementById('card-img');
//     imgEl.src = job.thumbnailUrl || 'https://via.placeholder.com/300';
//     imgEl.onerror = function() { this.src='https://via.placeholder.com/300?text=No+Image'; };
//
//     document.getElementById('card-title').innerText = job.title;
//     document.getElementById('card-address').innerText = job.address;
//     document.getElementById('card-phone').innerText = job.contactPhone || '-';
//
//     // 🌟 [핵심 수정] 자세히 보기 버튼 클릭 이벤트 연결
//     const detailBtn = document.getElementById('btn-detail');
//
//     detailBtn.onclick = function() {
//         // 현재 언어 설정 가져오기 (없으면 'kr')
//         const currentLang = new URLSearchParams(window.location.search).get('lang') || 'kr';
//
//         // 컨트롤러에 맞는 URL 생성 (/map/jobs/detail?id=...&source=...&lang=...)
//         // job.source가 DTO에 있으므로 반드시 넣어줘야 합니다!
//         const targetUrl = `/map/jobs/detail?id=${job.id}&source=${job.source}&lang=${currentLang}`;
//
//         // 페이지 이동 (새 창을 원하면 window.open(targetUrl) 사용)
//         window.location.href = targetUrl;
//     };
//
//     // 2. 카드 보여주기 & 바텀 시트 내리기
//     card.style.display = 'block';
//     $('#bottomSheet').removeClass('active');
// }
//
// // 🌟 [NEW] 카드 닫기 함수
// function closeJobCard() {
//     document.getElementById('jobDetailCard').style.display = 'none';
// }
//
//
// // [추가] 내 위치로 이동하는 함수
// function moveToCurrentLocation() {
//
//     // 1. 브라우저가 GPS를 지원하는지 확인
//     if (navigator.geolocation) {
//
//         // 로딩 중 표시 (선택사항)
//         // alert("위치를 찾는 중...");
//
//         navigator.geolocation.getCurrentPosition(
//             (position) => {
//                 // 2. 성공 시: 내 위도/경도 가져오기
//                 const pos = {
//                     lat: position.coords.latitude,
//                     lng: position.coords.longitude,
//                 };
//
//                 // 🌟 [NEW] 모드 스위치 ON!
//                 isLocationMode = true;
//
//                 // 3. 지도의 중심을 내 위치로 이동
//                 map.setCenter(pos);
//                 map.setZoom(15); // 주변을 잘 볼 수 있게 줌 확대
//
//                 // 4. (선택) 내 위치에 파란색 마커 표시 (나는 여기 있다!)
//                 new google.maps.Marker({
//                     position: pos,
//                     map: map,
//                     title: "내 위치",
//                     icon: {
//                         path: google.maps.SymbolPath.CIRCLE,
//                         scale: 10,
//                         fillColor: "#4285F4", // 구글 파란색
//                         fillOpacity: 1,
//                         strokeWeight: 2,
//                         strokeColor: "white",
//                     },
//                 });
//
//                 // 💡 참고: map.setCenter()가 실행되면 자동으로 'idle' 이벤트가 발생하여
//                 // loadJobs()가 실행되므로, 여기서 따로 loadJobs를 호출할 필요가 없습니다.
//             },
//             () => {
//                 // 실패 시 (사용자가 거부했거나 에러)
//                 handleLocationError(true, map.getCenter());
//             }
//         );
//     } else {
//         // 브라우저가 GPS 미지원
//         handleLocationError(false, map.getCenter());
//     }
// }
//
// // 에러 처리 함수
// function handleLocationError(browserHasGeolocation, pos) {
//     const infoWindow = new google.maps.InfoWindow({
//         map: map,
//         position: pos,
//         content: browserHasGeolocation
//             ? "에러: 위치 정보를 가져올 수 없습니다."
//             : "에러: 이 브라우저는 위치 정보를 지원하지 않습니다.",
//     });
// }
//
// // 🌟 [추가] 500m 원 그리기 함수
// function drawRadiusCircle(center) {
//     // 기존 원이 있으면 지움 (안 그러면 원이 계속 겹침)
//     if (radiusCircle) {
//         radiusCircle.setMap(null);
//     }
//
//     radiusCircle = new google.maps.Circle({
//         strokeColor: "#4285F4", // 테두리 파란색
//         strokeOpacity: 0.8,
//         strokeWeight: 2,
//         fillColor: "#4285F4",   // 내부 파란색
//         fillOpacity: 0.1,       // 아주 연하게
//         map: map,
//         center: center,
//         radius: 500, // 미터 단위 (500m)
//         clickable: false // 원 클릭 방지
//     });
// }
//
// // 🌟 [추가] 두 좌표 사이의 거리 계산 함수 (하버사인 공식)
// // 구글 맵 API에도 있지만, 계산 비용을 줄이기 위해 수학 공식 직접 사용
// function getDistanceFromLatLonInKm(lat1, lon1, lat2, lon2) {
//     const R = 6371; // 지구의 반지름 (km)
//     const dLat = deg2rad(lat2 - lat1);
//     const dLon = deg2rad(lon2 - lon1);
//     const a =
//         Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//         Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
//         Math.sin(dLon / 2) * Math.sin(dLon / 2);
//     const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//     const d = R * c; // 거리 (km)
//     return d;
// }
//
// function deg2rad(deg) {
//     return deg * (Math.PI / 180);
// }
//
// /* =======================================================================
// *                           좌표 관련 오류 발생시 로그 처리
// *
// *
// *
// * // (26/2/6) 프로젝트 구조 변경으로 인한 마커 미출력 문제로 코드 검토중
//             console.log("서버에서 받은 데이터: ",data);
//
//             if (data.length > 0){
//                 console.log("첫번째 데이터 샘플:", data[0]);
//                 console.log("JS가 찾는 좌표:", data[0].lat, data[0].lng);
//             }
//             * 위 내용을 fetch 내부에 삽입후 실행하면 데이터가 출력됨
// * */