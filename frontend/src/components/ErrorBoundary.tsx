import { Component, type ErrorInfo, type ReactNode } from 'react'

type Props = { children: ReactNode }

type State = { error: Error | null }

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  override componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('ErrorBoundary:', error, info.componentStack)
  }

  override render() {
    if (this.state.error) {
      return (
        <div className="mx-auto max-w-lg p-6 text-sm text-slate-800">
          <h1 className="mb-2 text-lg font-semibold">页面渲染出错</h1>
          <pre className="overflow-auto rounded-lg bg-red-50 p-3 text-red-800">{this.state.error.message}</pre>
          <p className="mt-4 text-slate-600">请刷新页面重试；若持续出现，请打开开发者工具 (F12) 查看 Console 并反馈。</p>
        </div>
      )
    }
    return this.props.children
  }
}
