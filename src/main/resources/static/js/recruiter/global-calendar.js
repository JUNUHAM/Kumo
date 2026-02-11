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
      timeZone: "local",

      dayCellContent: (arg) => {
        return { html: arg.date.getDate() };
      },

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

    // [다국어 적용] HTML에서 넘겨준 kumoMsgs 객체 사용
    const titleSuffix =
      typeof kumoMsgs !== "undefined" ? kumoMsgs.scheduleTitle : " 일정";
    if (titleEl) titleEl.innerText = dateStr + " " + titleSuffix;

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
      // [다국어 적용] 일정이 없을 때 메시지
      const emptyMsg =
        typeof kumoMsgs !== "undefined"
          ? kumoMsgs.noSchedule
          : "일정이 없습니다.";
      container.innerHTML = `<div class="empty-state text-center p-3">
                               <i class="bi bi-calendar-x d-block mb-2 fs-2 text-muted"></i>
                               ${emptyMsg}
                             </div>`;
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
