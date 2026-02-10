document.addEventListener("DOMContentLoaded", function () {
  const navEntries = performance.getEntriesByType("navigation")[0];
  const isReload = navEntries && navEntries.type === "reload";
  const hasVisited = sessionStorage.getItem("hasVisitedHome");

  if (isReload || !hasVisited) {
    // [A] 새로고침 또는 첫 방문: 클래스 추가해서 애니메이션 실행
    document.body.classList.add("start-animation");
    sessionStorage.setItem("hasVisitedHome", "true");
  } else {
    // [B] 메뉴 이동 시: 클래스를 제거하고 모든 효과를 'none'으로 강제 고정
    document.body.classList.remove("start-animation");

    const targets = [
      ".welcome-section",
      ".stat-row",
      ".graph-container",
      ".dashboard-right-sidebar",
    ];
    targets.forEach((selector) => {
      const el = document.querySelector(selector);
      if (el) {
        el.style.animation = "none";
        el.style.opacity = "1";
        el.style.transform = "none";
      }
    });
  }
});
