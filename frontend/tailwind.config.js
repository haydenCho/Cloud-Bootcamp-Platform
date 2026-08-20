/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,jsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        // 기본 sans 폰트를 Pretendard 로 (index.html 에서 CDN 로드)
        sans: [
          'Pretendard',
          'Pretendard Variable',
          'system-ui',
          '-apple-system',
          'sans-serif',
        ],
      },
      // CLAUDE.md 디자인 시스템 색상 팔레트
      colors: {
        accent: '#ECC815',    // 강조 / CTA
        dark: '#162326',      // 어두운 배경 / 텍스트
        primary: '#145D91',   // 메인 브랜드 컬러
        secondary: '#56A5DD', // 보조 색상
        light: '#77B4E4',     // 밝은 강조 / hover
      },
    },
  },
  plugins: [],
};
