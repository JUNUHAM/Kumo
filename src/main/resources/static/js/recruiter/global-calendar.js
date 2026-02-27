document.addEventListener("DOMContentLoaded", function () {
  const currentLang = document.documentElement.lang || "ko";

// 1. 메인 캘린더 영역
  const calendarEl = document.getElementById("calendar");
  if (calendarEl && typeof FullCalendar !== "undefined") {
    try {
      const calendar = new FullCalendar.Calendar(calendarEl, {
        // 🌟 사진처럼 주별 보기를 기본으로 하고 싶다면 'timeGridWeek'로 설정
        initialView: "dayGridMonth", 
        headerToolbar: {
          left: "prev,next today",
          center: "title",
          right: "dayGridMonth,timeGridWeek,timeGridDay,listWeek", // 🌟 Day(일별) 보기도 추가하면 좋습니다.
        },
        locale: currentLang,
        events: "/api/calendar/events",
        
        // 🌟 사진처럼 보이게 만드는 핵심 옵션들
        allDaySlot: true,            // 상단에 'all-day' 영역 표시 여부
        slotMinTime: "06:00:00",     // 시작 시간 (오전 6시)
        slotMaxTime: "20:00:00",     // 종료 시간 (오후 8시)
        expandRows: true,            // 화면 높이에 맞게 칸 늘리기
        slotEventOverlap: false,     // 일정끼리 겹치지 않고 옆으로 나열 (선택 사항)
        handleWindowResize: true,

        dateClick: function (info) {
          document.querySelectorAll(".selected-day").forEach((el) => el.classList.remove("selected-day"));
          info.dayEl.classList.add("selected-day");
          updateScheduleDetail(info.dateStr, calendar);
        },
        eventClick: function (info) {
          // 일정을 눌렀을 때 상세 내용을 보여주는 로직 (기존 유지)
          alert("일정: " + info.event.title + "\n내용: " + (info.event.extendedProps.description || "내용 없음"));
        },
      });
      calendar.render();
    } catch (e) { console.error("메인 캘린더 에러:", e); }
  }

  // 2. 미니 캘린더 영역
  const miniEl = document.getElementById("mini-calendar");
  if (miniEl && typeof FullCalendar !== "undefined") {
    const miniCalendar = new FullCalendar.Calendar(miniEl, {
      initialView: "dayGridMonth",
      locale: currentLang,
      headerToolbar: {
      left: 'prev',   // 왼쪽 버튼
      center: 'title', // 월/년 제목
      right: 'next'   // 오른쪽 버튼
      },
      height: "auto",
      events: "/api/calendar/events",

      // 점으로 표시, 최대 3개
      eventDisplay: "list-item",
      dayMaxEvents: 3,
      dayMaxEventRows: 3,
      // 🌟 [추가] 3개 넘어가면 무조건 리스트에서 빼버리는 로직
      eventDataTransform: function (eventData) {
    
      // 이 방법은 데이터 자체를 건드리는거라 복잡하니 패스하고, 
      // 아래 스타일 로직에서 display: none을 확실히 줍니다.
      return eventData;
      },
      
        // ★ 이벤트 로드 완료 후 자동 실행 → 오늘 날짜 선택 + 일정 표시
  eventsSet: function () {
    const now = new Date();
    const todayStr = getLocalDateStr(now);
    const dateToSelect = localStorage.getItem("selectedDate") || todayStr;

    // selected-day 클래스 적용
    setTimeout(() => {
      document.querySelectorAll(".fc-daygrid-day").forEach(el => {
        const dateAttr = el.getAttribute("data-date");
        if (dateAttr === dateToSelect) {
          el.classList.add("selected-day");
        }
      });
    }, 0);

    updateScheduleDetail(dateToSelect, miniCalendar);
  },

      dayCellContent: (arg) => ({ html: arg.date.getDate() }),

      // "+more" 링크 완전 숨김
      moreLinkContent: () => ({ html: "" }),
      moreLinkDidMount: (info) => {
        info.el.style.display = "none";
      },

      // 점 색상을 이벤트 색과 연동
      eventDidMount: function (info) {
    info.el.style.background = "none";
    info.el.style.border = "none";
    info.el.style.boxShadow = "none";
    info.el.style.padding = "0";
    info.el.style.margin = "0";

    // ★ 해당 날짜의 점 개수 카운트해서 3개 초과면 숨김
    const dayEl = info.el.closest(".fc-daygrid-day");
    if (dayEl) {
        const allDots = dayEl.querySelectorAll(".fc-daygrid-event-harness");
        const index = Array.from(allDots).indexOf(info.el.closest(".fc-daygrid-event-harness"));
        if (index >= 3) {
            info.el.closest(".fc-daygrid-event-harness").style.display = "none";
            return; // 이후 dot 스타일 적용 안 함
        }
    }

    const dot = info.el.querySelector(".fc-daygrid-event-dot");
    if (dot) {
        const color = info.event.backgroundColor || info.event.color || "#ff6b6b";
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
  }

  function getLocalDateStr(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
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
