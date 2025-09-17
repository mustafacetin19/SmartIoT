export default function FormShell({ title, subtitle, children, footer }) {
    return (
      <div className="panel">
        <div className="panel__head">
          <h2>{title}</h2>
          {subtitle && <p className="muted">{subtitle}</p>}
        </div>
        <div className="panel__body">{children}</div>
        {footer && <div className="panel__foot">{footer}</div>}
      </div>
    );
  }
  