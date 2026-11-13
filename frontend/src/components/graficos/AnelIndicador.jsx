// Anel de progresso do indicador de obrigações em dia (RN89, RN90)
export function AnelIndicador({ percentual, cor }) {
  const raio = 27;
  const circunferencia = 2 * Math.PI * raio;
  const valor = Math.max(0, Math.min(100, Number(percentual ?? 0)));

  return (
    <div className="anel" aria-hidden>
      <svg viewBox="0 0 64 64" width="64" height="64">
        <circle cx="32" cy="32" r={raio} fill="none" stroke="var(--cor-superficie-3)" strokeWidth="7" />
        {percentual != null && (
          <circle
            cx="32"
            cy="32"
            r={raio}
            fill="none"
            stroke={cor}
            strokeWidth="7"
            strokeLinecap="round"
            strokeDasharray={`${(valor / 100) * circunferencia} ${circunferencia}`}
          />
        )}
      </svg>
      <strong className="numero">{percentual != null ? `${Math.round(valor)}%` : '—'}</strong>
    </div>
  );
}
