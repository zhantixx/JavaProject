import React from 'react';

export function cn(...values) {
  return values.filter(Boolean).join(' ');
}

export function Surface({ className = '', children }) {
  return (
    <section
      className={cn(
        'rounded-2xl border border-[var(--border-color)] bg-[var(--panel-bg)] shadow-[var(--panel-shadow)] backdrop-blur',
        className
      )}
    >
      {children}
    </section>
  );
}

export function PageIntro({ eyebrow, title, description, actions, stats = [] }) {
  return (
    <Surface className="overflow-hidden">
      <div className="bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.18),_transparent_45%),linear-gradient(135deg,var(--brand-soft),transparent_70%)] p-6 md:p-8">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="max-w-2xl">
            {eyebrow ? (
              <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[var(--muted-text)]">
                {eyebrow}
              </p>
            ) : null}
            <h1 className="mt-2 text-3xl font-semibold text-[var(--text-primary)] md:text-4xl">{title}</h1>
            {description ? <p className="mt-3 max-w-xl text-sm text-[var(--text-secondary)] md:text-base">{description}</p> : null}
          </div>
          {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
        </div>
        {stats.length ? (
          <div className="mt-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {stats.map((stat) => {
              const valueText = String(stat.value ?? '');
              const compactValue = valueText.length > 18;

              return (
                <div
                  key={stat.label}
                  className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] px-4 py-4"
                >
                  <div className="text-xs font-medium uppercase tracking-[0.12em] text-[var(--muted-text)]">
                    {stat.label}
                  </div>
                  <div
                    className={cn(
                      'mt-2 font-semibold leading-tight text-[var(--text-primary)]',
                      compactValue ? 'break-all text-sm sm:text-base' : 'text-2xl'
                    )}
                  >
                    {stat.value}
                  </div>
                  {stat.hint ? <div className="mt-1 text-xs text-[var(--text-secondary)]">{stat.hint}</div> : null}
                </div>
              );
            })}
          </div>
        ) : null}
      </div>
    </Surface>
  );
}

export function SectionTitle({ title, description, aside }) {
  return (
    <div className="flex flex-col gap-3 border-b border-[var(--border-color)] px-5 py-5 md:flex-row md:items-start md:justify-between">
      <div>
        <h2 className="text-lg font-semibold text-[var(--text-primary)]">{title}</h2>
        {description ? <p className="mt-1 text-sm text-[var(--text-secondary)]">{description}</p> : null}
      </div>
      {aside ? <div className="shrink-0">{aside}</div> : null}
    </div>
  );
}

export function GhostButton({ className = '', ...props }) {
  return (
    <button
      {...props}
      className={cn(
        'rounded-xl border border-[var(--border-color)] bg-[var(--card-strong)] px-4 py-2.5 text-sm font-medium text-[var(--text-primary)] transition hover:border-[var(--brand-border)] hover:text-[var(--brand-strong)]',
        className
      )}
    />
  );
}

export function PrimaryButton({ className = '', ...props }) {
  return (
    <button
      {...props}
      className={cn(
        'rounded-xl bg-[var(--brand-strong)] px-4 py-2.5 text-sm font-medium text-white transition hover:bg-[var(--brand-stronger)] disabled:cursor-not-allowed disabled:opacity-60',
        className
      )}
    />
  );
}
