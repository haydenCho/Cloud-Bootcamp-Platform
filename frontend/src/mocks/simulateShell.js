/**
 * ⚠️ 프론트엔드 전용 가짜 셸 출력 생성기 — 실제 셸을 실행하지 않는다.
 *
 * 백엔드는 정규식 패턴 검증만 하고(CLAUDE.md 원칙), 정답으로 판정된 명령에 대해
 * 이 함수가 "그럴듯한" 출력 텍스트를 만들어 가짜 터미널에 보여준다.
 * SHELL 미션 상세에서 정답일 때만 호출된다.
 *
 * @param {string} command 사용자가 입력한(정답으로 판정된) 명령
 * @returns {string[]} 출력 줄 배열 (출력이 없는 명령은 빈 배열)
 */
export function simulateShell(command) {
  const cmd = (command || '').trim().replace(/\s+/g, ' ');

  // ls: -a/--all 이면 숨김 파일 포함
  if (/^ls\b/.test(cmd)) {
    const showHidden = /\s-\S*a/.test(cmd) || /--all\b/.test(cmd);
    const visible = ['deploy.sh', 'project', 'README.md', 'src'];
    const hidden = ['.', '..', '.bashrc', '.env', '.gitignore'];
    const files = showHidden ? [...hidden, ...visible] : visible;
    return [files.join('  ')];
  }

  // pwd
  if (/^pwd\b/.test(cmd)) {
    return ['/home/guest/workspace'];
  }

  // chmod / mkdir 은 성공 시 표준 출력이 없다 (완료 배너로 성공을 표시)
  if (/^chmod\b/.test(cmd) || /^mkdir\b/.test(cmd)) {
    return [];
  }

  return [];
}
