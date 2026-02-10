(function () {
  const savedTheme = localStorage.getItem("theme");
  if (savedTheme === "dark") {
    // CSS 파일을 다 읽기 전에 body에 클래스를 미리 박아버립니다.
    document.documentElement.classList.add("dark-mode");
  }
})();
