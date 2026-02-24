/**
 * search_job_list.js
 * 기능: 필터 조작, URL 파라미터 파싱, AJAX 검색 요청, 테이블 렌더링
 */

// 지역 데이터 구성
const RegionData = {
    tokyo: ["신주쿠구", "시부야구", "미나토구", "추오구", "에도가와구"],
    osaka: ["기타구", "주오구", "나니와구", "덴노지구", "니시나리구"]
};

// =========================================
// [1] 초기화 (Document Ready)
// =========================================
$(document).ready(function() {
    // 1. URL에서 넘어온 키워드 파라미터 읽기 (지역은 안 읽음!)
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword') || '';

    // 2. 검색창에 넘어온 키워드 꽂아넣기
    $('#keywordInput').val(keyword);

    // 3. 서브 지역 세팅
    updateSubRegions();

    // 4. 페이지 진입하자마자 AJAX 검색 1회 실행
    SearchService.fetchList();

    // 5. 이벤트 바인딩 (클릭 & 엔터키)
    $('#mainRegion').on('change', updateSubRegions);
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
    $subSelect.append('<option value="">전체 구/시</option>');

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

        // 로딩 UI 표시
        $('#searchListBody').html(`<tr><td colspan="8" style="text-align:center; padding: 40px;">검색 중...</td></tr>`);

        // 🌟 AJAX 요청
        $.ajax({
            url: '/map/api/jobs/search', // 실제 백엔드 검색 API 주소로 변경하세요!
            method: 'GET',
            data: {
                keyword: keyword,
                mainRegion: mainRegion,
                subRegion: subRegion,
                lang: currentLang
            },
            dataType: 'json',
            success: function(response) {
                // response가 배열 형태라고 가정
                SearchService.renderTable(response);
            },
            error: function(xhr, status, error) {
                console.error("검색 실패:", error);
                $('#searchListBody').html(`<tr><td colspan="8" style="text-align:center; padding: 40px; color: red;">데이터를 불러오는데 실패했습니다.</td></tr>`);
            }
        });
    },

    renderTable: function(jobs) {
        const $tbody = $('#searchListBody');

        if (!jobs || jobs.length === 0) {
            $tbody.html(`<tr><td colspan="8" style="text-align:center; padding: 40px; color: #888;">검색 결과가 없습니다.</td></tr>`);
            return;
        }

        let html = '';
        jobs.forEach(job => {
            // 더미 렌더링 (실제 필드명에 맞게 수정 필요)
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
                <td><div class="wage-box"><span class="wage-type">시급</span><span class="wage-amount">${job.wage || '-'}</span></div></td>
                <td class="text-muted">${job.contactPhone || '-'}</td>
                <td>
                    <div class="author-box">
                        <img src="${job.thumbnailUrl || 'https://placehold.co/30'}" class="author-img">
                        <div class="author-info">
                            <span class="author-name">${job.manager || '담당자'}</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="action-buttons">
                        <button class="btn-outline">찜하기</button>
                        <button class="btn-filled" onclick="location.href='/map/jobs/detail?id=${job.id}'">상세보기</button>
                    </div>
                </td>
                <td class="text-muted">방금 전</td>
            </tr>`;
        });

        $tbody.html(html);
    }
};