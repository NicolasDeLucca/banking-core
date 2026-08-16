export function ErrorMessage({ error }) {
  if (!error) return null;
  return (
    <p className="error-message">
      {error.code ? `${error.code}: ` : ""}
      {error.message}
    </p>
  );
}
