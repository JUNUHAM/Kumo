// 지도 생성
let map;

// Jquery를 사용하면 오히려 번거롭기 때문에 Vanilla JS를 사용하여 map 구축
function initMap() {
    const mapElement = document.querySelector('.map-container');
    if (!mapElement) return;

    const tokyo = { lat: 35.6804, lng: 139.7690 };

    map = new google.maps.Map(mapElement, {
        center: tokyo,
        zoom: 14,
    });
}

// HTML 문서가 다 로딩되면 실행 (jQuery의 ready 함수) <- Jquery 사용
$(function() {
    // 1. '.sheet-handle' 클래스를 가진 요소를 클릭하면
    $('.sheet-handle').on('click', function() {

        // 2. '#bottomSheet' 아이디를 가진 요소에 'active' 클래스를 줬다 뺏었다 함
        $('#bottomSheet').toggleClass('active');

    });
});

// 전역 등록
window.initMap = initMap;