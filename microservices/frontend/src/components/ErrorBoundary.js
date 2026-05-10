import React from 'react';

export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }
  componentDidCatch(error, errorInfo) {
    // можно добавить логирование
    // eslint-disable-next-line no-console
    console.error('UI Error:', error, errorInfo);
  }
  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
          <div className="max-w-lg w-full bg-white rounded-xl shadow p-8 text-center">
            <div className="mx-auto w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mb-4">
              <span className="text-red-600 text-3xl">!</span>
            </div>
            <h1 className="text-2xl font-semibold text-gray-900 mb-2">Что-то пошло не так</h1>
            <p className="text-gray-600 mb-6">
              Произошла непредвиденная ошибка интерфейса. Попробуйте обновить страницу или вернуться позже.
            </p>
            <pre className="text-left text-sm bg-gray-100 p-3 rounded overflow-auto max-h-40">
              {String(this.state.error || '')}
            </pre>
            <button onClick={() => window.location.reload()}
                    className="mt-6 inline-flex items-center px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700">
              Обновить страницу
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
