/**
 * search_job_list.js
 * 기능: 필터 조작, URL 파라미터 파싱, AJAX 검색 요청, 테이블 렌더링
 */

// =========================================
// [1] 초기화 (Document Ready)
// =========================================
$(document).ready(function() {
    // 1. URL에서 넘어온 파라미터 싹 다 읽기 (새로고침 시 필터 유지용)
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword') || '';
    const mainRegion = urlParams.get('mainRegion') || 'tokyo'; // 기본값 도쿄
    const subRegion = urlParams.get('subRegion') || '';

    // 2. 검색창 및 지역 셀렉트 박스에 기존 값 꽂아넣기
    $('#keywordInput').val(keyword);
    $('#mainRegion').val(mainRegion);

    // 3. 서브 지역 세팅 후, URL에 있던 구(subRegion)로 자동 선택 맞추기
    updateSubRegions();
    if (subRegion) {
        $('#subRegion').val(subRegion);
    }

    // 4. 페이지 진입하자마자 AJAX 검색 1회 실행
    SearchService.fetchList();

    // 5. 이벤트 바인딩
    $('#mainRegion').on('change', function() {
        updateSubRegions();
        // 지역(도쿄/오사카)을 바꿀 때 서브 지역이 초기화되므로 바로 검색을 한 번 때려줍니다.
        SearchService.fetchList();
    });

    // 서브 지역(구)을 바꿀 때도 굳이 검색 버튼 안 누르고 바로 리스트가 바뀌게 하려면 아래 주석을 풀어주세요! (UX 꿀팁)
    $('#subRegion').on('change', SearchService.fetchList);

    $('#btnSearch').on('click', SearchService.fetchList);
    $('#keywordInput').on('keyup', function(e) {
        if (e.key === 'Enter') SearchService.fetchList();
    });
});

// =========================================
// [2] UI 동작 로직
// =========================================
function updateSubRegions() {
    const mainRegion = $('#mainRegion').val();
    const $subSelect = $('#subRegion');

    $subSelect.empty();
    $subSelect.append(`<option value="">${LIST_MESSAGES.allRegion}</option>`);

    if (RegionData[mainRegion]) {
        RegionData[mainRegion].forEach(sub => {
            $subSelect.append(`<option value="${sub}">${sub}</option>`);
        });
    }
}

// =========================================
// [3] AJAX 및 데이터 렌더링 (SearchService)
// =========================================
const SearchService = {
    fetchList: function() {
        const keyword = $('#keywordInput').val().trim();
        const mainRegion = $('#mainRegion').val();
        const subRegion = $('#subRegion').val();
        const currentLang = new URLSearchParams(window.location.search).get('lang') || 'kr';

        // 🌟 [핵심 UX 추가] 검색 버튼을 누르면 URL 창의 주소도 새로고침 없이 싹 바꿔줍니다.
        // 나중에 유저가 이 링크를 복사해서 친구한테 공유해도 필터가 그대로 유지됩니다!
        const newUrl = `/map/search_list?lang=${currentLang}&mainRegion=${mainRegion}&subRegion=${encodeURIComponent(subRegion)}&keyword=${encodeURIComponent(keyword)}`;
        window.history.pushState(null, '', newUrl);

        // 로딩 메시지도 다국어 적용
        $('#searchListBody').html(`<tr><td colspan="7" style="text-align:center; padding: 40px;">${LIST_MESSAGES.loading}</td></tr>`);

        // 🌟 AJAX 요청
        $.ajax({
            url: '/map/api/jobs/search',
            method: 'GET',
            data: {
                keyword: keyword,
                mainRegion: mainRegion,
                subRegion: subRegion,
                lang: currentLang
            },
            dataType: 'json',
            success: function(response) {
                SearchService.renderTable(response);
            },
            error: function(xhr, status, error) {
                console.error("검색 실패:", error);
                $('#searchListBody').html(`<tr><td colspan="7" style="text-align:center; padding: 40px; color: red;">${LIST_MESSAGES.error}</td></tr>`);
            }
        });
    },

    renderTable: function(jobs) {
        const $tbody = $('#searchListBody');

        if (!jobs || jobs.length === 0) {
            $tbody.html(`<tr><td colspan="7" style="text-align:center; padding: 40px; color: #888;">${LIST_MESSAGES.empty}</td></tr>`);
            return;
        }

        let html = '';
        jobs.forEach(job => {
            // 🌟 1. 로그인 여부에 따라 찜하기 버튼 생성/숨김
            const saveBtnHtml = isUserLoggedIn
                ? `<button class="btn-outline">${LIST_MESSAGES.saveBtn}</button>`
                : '';

            html += `
            <tr>
                <td>
                    <div class="job-title-cell">
                        <span class="job-title-text">${job.title || '제목 없음'}</span>
                        <div class="badges">
                            <span class="badge badge-green">식품제조</span>
                        </div>
                    </div>
                </td>
                <td class="text-blue font-weight-bold">${job.companyName || '-'}</td>
                <td>${job.address || '-'}</td>
                <td>
                    <div class="wage-box">
                        <span class="wage-type">${LIST_MESSAGES.wageType}</span>
                        <span class="wage-amount">${job.wage || '-'}</span>
                    </div>
                </td>
                <td class="text-muted">${job.contactPhone || '-'}</td>
                <td>
                    <div class="author-box">
                        <img src="${job.thumbnailUrl || 'https://placehold.co/30'}" class="author-img">
                        <div class="author-info">
                            <span class="author-name">${job.manager || LIST_MESSAGES.manager}</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="action-buttons">
                        ${saveBtnHtml}
                        <button class="btn-filled" onclick="location.href='/map/jobs/detail?id=${job.id}'">${LIST_MESSAGES.detailBtn}</button>
                    </div>
                </td>
            </tr>`;
        });

        $tbody.html(html);
    }
};