document.addEventListener("DOMContentLoaded", function () {
  const currentLang = document.documentElement.lang || "ko";

  // 1. 메인 캘린더 영역
  const calendarEl = document.getElementById("calendar");
  if (calendarEl && typeof FullCalendar !== "undefined") {
    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: "dayGridMonth",
      headerToolbar: {
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,timeGridWeek,listWeek",
      },
      locale: currentLang,
      events: "/api/calendar/events",
      dateClick: function (info) {
        document
          .querySelectorAll(".selected-day")
          .forEach((el) => el.classList.remove("selected-day"));
        info.dayEl.classList.add("selected-day");
        updateScheduleDetail(info.dateStr, calendar);
      },
      eventClick: function (info) {
        alert(
          "일정: " +
            info.event.title +
            "\n내용: " +
            (info.event.extendedProps.description || "내용 없음"),
        );
      },
    });
    calendar.render();
  }

  // 2. 미니 캘린더 영역
  const miniEl = document.getElementById("mini-calendar");
  if (miniEl && typeof FullCalendar !== "undefined") {
    const now = new Date();
    const todayStr = now.toISOString().split("T")[0];
    const dateToSelect = localStorage.getItem("selectedDate") || todayStr;

    const miniCalendar = new FullCalendar.Calendar(miniEl, {
      initialView: "dayGridMonth",
      locale: currentLang,
      headerToolbar: false,
      height: "auto",
      events: "/api/calendar/events",

      // 점으로 표시, 최대 3개
      eventDisplay: "list-item",
      dayMaxEvents: 3,
      dayMaxEventRows: 3,

      dayCellContent: (arg) => ({ html: arg.date.getDate() }),

      // "+more" 링크 완전 숨김
      moreLinkContent: () => ({ html: "" }),
      moreLinkDidMount: (info) => {
        info.el.style.display = "none";
      },

      // 점 색상을 이벤트 색과 연동
      eventDidMount: function (info) {
        // 이벤트 자체 배경/테두리 제거
        info.el.style.background = "none";
        info.el.style.border = "none";
        info.el.style.boxShadow = "none";
        info.el.style.padding = "0";
        info.el.style.margin = "0";

        const dot = info.el.querySelector(".fc-daygrid-event-dot");
        if (dot) {
          const color =
            info.event.backgroundColor || info.event.color || "#7abaff";
          dot.style.setProperty("background-color", color, "important");
          dot.style.setProperty("border-color", color, "important");
          dot.style.setProperty("width", "6px", "important");
          dot.style.setProperty("height", "6px", "important");
          dot.style.setProperty("border-radius", "50%", "important");
          dot.style.setProperty("border", "none", "important");
          dot.style.setProperty("display", "block", "important");
          dot.style.setProperty("visibility", "visible", "important");
          dot.style.setProperty("flex-shrink", "0", "important");
        }
      },

      dateClick: function (info) {
        localStorage.setItem("selectedDate", info.dateStr);
        document
          .querySelectorAll(".selected-day")
          .forEach((el) => el.classList.remove("selected-day"));
        info.dayEl.classList.add("selected-day");
        updateScheduleDetail(info.dateStr, miniCalendar);
      },
    });
    miniCalendar.render();
    updateScheduleDetail(dateToSelect, miniCalendar);
  }

  // 3. 상세 일정 업데이트 함수
  function updateScheduleDetail(dateStr, calendarApi) {
    const container = document.getElementById("event-list-container");
    if (!container) return;

    const titleEl = document.getElementById("selected-date-title");
    const titleSuffix =
      typeof kumoMsgs !== "undefined" ? kumoMsgs.scheduleTitle : " 일정";
    if (titleEl) titleEl.innerText = dateStr + " " + titleSuffix;

    // ★ 시차 오류 수정: toISOString()은 UTC 기준이라 한국(UTC+9)에서 날짜가 밀림
    //    → 로컬 시간 기준으로 YYYY-MM-DD 문자열 생성
    function getLocalDateStr(date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    }

    const events = calendarApi.getEvents().filter((e) => {
      return getLocalDateStr(e.start) === dateStr;
    });

    container.innerHTML = "";
    if (events.length === 0) {
      const emptyMsg =
        typeof kumoMsgs !== "undefined"
          ? kumoMsgs.noSchedule
          : "일정이 없습니다.";
      container.innerHTML = `<div class="empty-state text-center p-3">${emptyMsg}</div>`;
    } else {
      events.forEach((e) => {
        const card = document.createElement("div");
        card.className = "sidebar-card";

        const eventColor = e.backgroundColor || e.color || "#7abaff";
        card.style.setProperty("border-left-color", eventColor, "important");

        const timeStr = e.start.toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        });

        card.innerHTML = `
          <div style="display: flex; align-items: center; gap: 12px; width: 100%; padding: 2px 0;">
            <div class="event-item-title" style="margin: 0; font-size: 0.9rem; font-weight: 700;">${e.title}</div>
            <div class="event-item-time" style="font-size: 0.8rem; color: #8b95a1; white-space: nowrap;">
              <i class="bi bi-clock me-1"></i>${timeStr}
            </div>
          </div>`;
        container.appendChild(card);
      });
    }
  }
});
