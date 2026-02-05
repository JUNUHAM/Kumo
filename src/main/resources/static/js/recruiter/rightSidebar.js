document.addEventListener("DOMContentLoaded", function () {
  // 오늘 날짜 정보 가져오기
  const now = new Date();

  // 영문 월 이름 배열 (원하시는 형식에 따라 한글/영문 선택 가능)
  const monthNames = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];

  const currentMonth = monthNames[now.getMonth()]; // 예: February
  const currentYear = now.getFullYear(); // 예: 2026

  // 해당 영역에 텍스트 주입
  const monthTitleEl = document.getElementById("current-month-title");
  if (monthTitleEl) {
    monthTitleEl.innerText = `${currentMonth}, ${currentYear}`;
  }
});
