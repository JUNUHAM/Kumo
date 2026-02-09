document.addEventListener("DOMContentLoaded", function () {
  const miniEl = document.getElementById("mini-calendar");

  if (miniEl && typeof FullCalendar !== "undefined") {
    const miniCalendar = new FullCalendar.Calendar(miniEl, {
      initialView: "dayGridMonth",
      locale: "ko",
      headerToolbar: false,
      height: "auto",
      contentHeight: "auto",
      timeZone: "local",

      // [중요] 중복 제거 및 숫자만 표시 (하나만 남겨야 합니다)
      dayCellContent: (arg) => {
        return { html: arg.date.getDate() };
      },

      events: [
        {
          title: "면접(김철수)",
          start: "2026-02-10T10:00:00",
          allDay: false, // 점으로 표시하기 위해 필수
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
        const prevSelected = document.querySelectorAll(".selected-day");
        prevSelected.forEach((el) => el.classList.remove("selected-day"));

        info.dayEl.classList.add("selected-day");
        console.log("선택한 날짜: " + info.dateStr);
        updateScheduleDetail(info.dateStr, miniCalendar);
      },
    });

    miniCalendar.render();
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
      container.innerHTML = `
        <div class="empty-state text-center p-3">
          <i class="bi bi-calendar-x d-block mb-2 fs-2 text-muted"></i>
          일정이 없습니다.
        </div>`;
    } else {
      events.forEach((e) => {
        const card = document.createElement("div");
        card.className =
          "sidebar-card p-3 mb-2 border rounded shadow-sm bg-white";
        card.style.borderLeft = `5px solid ${e.backgroundColor || "#7abaff"}`;
        card.innerHTML = `<div class="fw-bold">${e.title}</div>`;
        container.appendChild(card);
      });
    }
  }
});
