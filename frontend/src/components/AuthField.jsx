/** 인증 폼용 라벨 + 입력 필드 (light 톤). */
export default function AuthField({ label, type = 'text', value, onChange, placeholder, autoComplete }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-dark/70">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-dark
                   outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
      />
    </label>
  );
}
