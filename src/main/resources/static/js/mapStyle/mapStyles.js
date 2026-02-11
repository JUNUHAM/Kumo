// [0] 지도 스타일 정의 (Light / Dark)
const MapStyles = {
    // ☀️ 라이트 모드 (기존 스타일: 채도 -20)
    light: [
        {
            "featureType": "all",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "all",
            "elementType": "geometry.fill",
            "stylers": [{ "hue": "#00A8FF" }]
        },
        {
            "featureType": "all",
            "elementType": "geometry.stroke",
            "stylers": [{ "color": "#483E96" }]
        },
        {
            "featureType": "administrative",
            "elementType": "all",
            "stylers": [{ "hue": "#FF0000" }]
        },
        {
            "featureType": "administrative",
            "elementType": "geometry.fill",
            "stylers": [
                { "saturation": "23" },
                { "visibility": "on" },
                { "color": "#FF0000" }
            ]
        },
        {
            "featureType": "administrative.country",
            "elementType": "all",
            "stylers": [{ "visibility": "off" }]
        },
        {
            "featureType": "administrative.locality",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "landscape.man_made",
            "elementType": "all",
            "stylers": [
                { "visibility": "on" },
                { "color": "#E8F9FF" }
            ]
        },
        {
            "featureType": "landscape.man_made",
            "elementType": "geometry.stroke",
            "stylers": [
                { "color": "#3975FF" },
                { "invert_lightness": true },
                { "visibility": "on" }
            ]
        },
        {
            "featureType": "landscape.man_made",
            "elementType": "labels.text.fill",
            "stylers": [{ "color": "#455DB8" }]
        },
        {
            "featureType": "landscape.natural",
            "elementType": "all",
            "stylers": [{ "color": "#A8C0D8" }]
        },
        {
            "featureType": "poi.attraction",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "poi.business",
            "elementType": "all",
            "stylers": [{ "visibility": "off" }]
        },
        {
            "featureType": "poi.medical",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "poi.medical",
            "elementType": "labels.icon",
            "stylers": [{ "color": "#E1C320" }]
        },
        {
            "featureType": "poi.park",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "poi.park",
            "elementType": "labels.icon",
            "stylers": [{ "color": "#50C36B" }]
        },
        {
            "featureType": "poi.place_of_worship",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "poi.place_of_worship",
            "elementType": "labels.icon",
            "stylers": [{ "color": "#C063DC" }]
        },
        {
            "featureType": "poi.school",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "poi.school",
            "elementType": "labels.icon",
            "stylers": [{ "color": "#E1C320" }]
        },
        {
            "featureType": "road",
            "elementType": "geometry.stroke",
            "stylers": [{ "color": "#0046E6" }]
        },
        {
            "featureType": "road.highway",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "road.highway",
            "elementType": "geometry.fill",
            "stylers": [{ "color": "#A4C5E4" }]
        },
        {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [{ "color": "#273473" }]
        },
        {
            "featureType": "road.local",
            "elementType": "all",
            "stylers": [{ "visibility": "on" }]
        },
        {
            "featureType": "water",
            "elementType": "all",
            "stylers": [{ "color": "#BDD7FF" }]
        }
    ],

    // 🌙 다크 모드 (작성해주신 JSON)
    dark: [
        {
            "featureType": "landscape",
            "elementType": "geometry.fill",
            "stylers": [{ "color": "#262424" }]
        },
        {
            "featureType": "landscape",
            "elementType": "geometry.stroke",
            "stylers": [{ "color": "#FFFFFF" }]
        },
        {
            "featureType": "landscape",
            "elementType": "labels.text",
            "stylers": [{ "color": "#FFFFFF" }]
        },
        {
            "featureType": "poi.park",
            "elementType": "geometry.fill",
            "stylers": [{ "color": "#399030" }]
        },
        {
            "featureType": "road",
            "elementType": "geometry.fill",
            "stylers": [
                { "visibility": "on" },
                { "color": "#324959" }
            ]
        },
        {
            "featureType": "transit.line",
            "elementType": "geometry.fill",
            "stylers": [{ "color": "#D9D9D9" }]
        },
        {
            "featureType": "water",
            "elementType": "geometry.fill",
            "stylers": [{ "color": "#1F3350" }]
        }
    ]
};