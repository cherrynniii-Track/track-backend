import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const firstPageDuration = new Trend('first_page_duration');
const statusDifficultyDuration = new Trend('status_difficulty_duration');
const categoryDuration = new Trend('category_duration');
const deepPageDuration = new Trend('deep_page_duration');

// 조회 조건별 응답 시간을 분리해서 측정하기 위한 지표
export const options = {
    stages: [
        // 10초 동안 가상 사용자 10명까지 증가
        { duration: '10s', target: 10 },
        // 가상 사용자 10명을 30초 동안 유지
        { duration: '30s', target: 10 },
        // 10초 동안 가상 사용자를 0명까지 감소
        { duration: '10s', target: 0 },
    ],
    
    // 테스트 결과에 표시할 응답 시간 통계
    summaryTrendStats: [
        'avg',
        'min',
        'med',
        'max',
        'p(90)',
        'p(95)',
        'p(99)',
    ],
};

const BASE_URL = 'http://localhost:8080';
const PROJECT_ID = 7;

// 부하 테스트에 사용할 Task 목록 조회 조건
const requests = [
    {
        name: 'first-page',
        query: '?page=0&size=20',
        metric: firstPageDuration,
    },
    {
        name: 'status-difficulty',
        query: '?status=IN_PROGRESS&difficulty=NORMAL&page=0&size=20',
        metric: statusDifficultyDuration,
    },
    {
        name: 'category',
        query: '?categoryId=2&page=0&size=20',
        metric: categoryDuration,
    },
    {
        name: 'deep-page',
        query: '?page=2000&size=20',
        metric: deepPageDuration,
    },
];

export default function () {
    // 네 가지 조회 조건을 순서대로 반복 실행
    const request = requests[__ITER % requests.length];

    const response = http.get(
        `${BASE_URL}/api/projects/${PROJECT_ID}/tasks${request.query}`,
        {
            // 실행할 때 환경변수로 전달받은 access token 사용
            headers: {
                Authorization: `Bearer ${__ENV.ACCESS_TOKEN}`,
            },
            // k6 결과에서 요청 종류를 구분하기 위한 이름
            tags: {
                name: request.name,
            },
        }
    );

    // 현재 조회 조건의 응답 시간을 해당 지표에 기록
    request.metric.add(response.timings.duration);

    // API가 정상적으로 응답했는지 검증
    check(response, {
        [`${request.name}: status is 200`]: (res) => res.status === 200,
        [`${request.name}: returns 20 tasks`]: (res) =>
            res.json().content?.length === 20,
    });

    // 실제 사용자의 요청 간격을 가정해 1초 대기
    sleep(1);
}