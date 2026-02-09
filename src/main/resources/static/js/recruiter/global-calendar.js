document.addEventListener("DOMContentLoaded", function () {
  const miniEl = document.getElementById("mini-calendar");

  if (miniEl && typeof FullCalendar !== "undefined") {
    // [수정] 한국 시간 기준으로 오늘 날짜 구하기 (밀림 방지)
    const now = new Date();
    const todayStr =
      now.getFullYear() +
      "-" +
      String(now.getMonth() + 1).padStart(2, "0") +
      "-" +
      String(now.getDate()).padStart(2, "0");

    // 저장된 날짜 확인
    const savedDate = localStorage.getItem("selectedDate");
    const dateToSelect = savedDate || todayStr;

    const miniCalendar = new FullCalendar.Calendar(miniEl, {
      initialView: "dayGridMonth",
      locale: "ko",
      headerToolbar: false,
      height: "auto",
      contentHeight: "auto",
      timeZone: "local", // 로컬 타임존 사용

      dayCellContent: (arg) => {
        return { html: arg.date.getDate() };
      },

      // 셀이 생성될 때 저장된 날짜와 비교하여 즉시 클래스 부여 (딜레이 제로)
      dayCellDidMount: function (arg) {
        const d = arg.date;
        const cellDate =
          d.getFullYear() +
          "-" +
          String(d.getMonth() + 1).padStart(2, "0") +
          "-" +
          String(d.getDate()).padStart(2, "0");

        if (cellDate === dateToSelect) {
          arg.el.classList.add("selected-day");
        }
      },

      events: [
        {
          title: "면접(김철수)",
          start: "2026-02-10T10:00:00",
          allDay: false,
          color: "#7abaff",
        },
        {
          title: "회의",
          start: "2026-02-14T14:00:00",
          allDay: false,
          color: "#92ccff",
        },
      ],

      dateClick: function (info) {
        localStorage.setItem("selectedDate", info.dateStr);

        document.querySelectorAll(".selected-day").forEach((el) => {
          el.classList.remove("selected-day");
        });

        info.dayEl.classList.add("selected-day");
        updateScheduleDetail(info.dateStr, miniCalendar);
      },
    });

    miniCalendar.render();
    updateScheduleDetail(dateToSelect, miniCalendar);
  }

  function updateScheduleDetail(dateStr, calendarApi) {
    const titleEl = document.getElementById("selected-date-title");
    if (titleEl) titleEl.innerText = dateStr + " 일정";

    const events = calendarApi.getEvents().filter((e) => {
      const d = e.start;
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}` === dateStr;
    });

    const container = document.getElementById("event-list-container");
    if (!container) return;

    container.innerHTML = "";

    if (events.length === 0) {
      container.innerHTML = `<div class="empty-state text-center p-3"><i class="bi bi-calendar-x d-block mb-2 fs-2 text-muted"></i>일정이 없습니다.</div>`;
    } else {
      events.forEach((e) => {
        const card = document.createElement("div");
        card.className = "sidebar-card";
        card.style.borderLeft = `5px solid ${e.backgroundColor || "#7abaff"}`;
        card.innerHTML = `<div class="fw-bold">${e.title}</div>`;
        container.appendChild(card);
      });
    }
  }
});
