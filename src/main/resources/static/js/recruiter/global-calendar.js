document.addEventListener("DOMContentLoaded", function () {
  const currentLang = document.documentElement.lang || "ko"; // HTML lang 속성 참고

  // 1. 메인 캘린더 영역 (페이지에 있을 때만 실행)
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

  // 2. 미니 캘린더 영역 (사이드바 등에 있을 때만 실행)
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

      // [최종] 점을 무조건 그리라고 강제하는 3대장
      eventDisplay: "list-item",
      dayMaxEvents: 3, // ⚠️ false로 두어야 공간 부족해도 점을 안 숨깁니다.
      dayMaxEventRows: false,

      dayCellContent: (arg) => ({ html: arg.date.getDate() }),

      // [최종] 점에 색깔 입히고 강제로 깨우기
      eventDidMount: function (info) {
        const dot = info.el.querySelector(".fc-daygrid-event-dot");
        if (dot) {
          dot.style.setProperty(
            "background-color",
            info.event.backgroundColor || info.event.color || "#7abaff",
            "important",
          );
          dot.style.setProperty("display", "block", "important");
          dot.style.setProperty("visibility", "visible", "important");
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

  // 3. 상세 일정 업데이트 함수 (공통 사용)
  // 3. 상세 일정 업데이트 함수 (시간 표시 추가 버전)
  function updateScheduleDetail(dateStr, calendarApi) {
    const container = document.getElementById("event-list-container");
    if (!container) return;

    const titleEl = document.getElementById("selected-date-title");
    const titleSuffix =
      typeof kumoMsgs !== "undefined" ? kumoMsgs.scheduleTitle : " 일정";
    if (titleEl) titleEl.innerText = dateStr + " " + titleSuffix;

    const events = calendarApi.getEvents().filter((e) => {
      const d = e.start;
      return d.toISOString().split("T")[0] === dateStr;
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

        // [핵심] 다크모드 !important를 뚫고 색상을 박아넣는 로직
        const eventColor = e.backgroundColor || e.color || "#7abaff";
        card.style.setProperty("border-left-color", eventColor, "important");

        const timeStr = e.start.toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        });

        // [완료] 한 줄 배치 + 화살표(>) 삭제 + 슬림한 높이
        card.innerHTML = `
        <div style="display: flex; align-items: center; gap: 12px; width: 100%; padding: 2px 0;">
            <div class="event-item-title" style="margin: 0; font-size: 0.9rem; font-weight: 700;">${e.title}</div>
            <div class="event-item-time" style="font-size: 0.8rem; color: #8b95a1; white-space: nowrap;">
                <i class="bi bi-clock me-1"></i>${timeStr}
            </div>
        </div>
    `;
        container.appendChild(card);
      });
    }
  }
});
