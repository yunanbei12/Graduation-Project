export function sanitizeRichText(html) {
  if (!html || typeof html !== 'string') return ''
  return html
    .replace(/<!--[\s\S]*?-->/gi, '')
    .replace(/<\s*(script|style|iframe|frame|object|embed|link|meta|base|form|input|button|textarea|select)[^>]*>[\s\S]*?<\s*\/\s*\1\s*>/gi, '')
    .replace(/<\s*(script|style|iframe|frame|object|embed|link|meta|base|input)[^>]*\/?>/gi, '')
    .replace(/\s+on[a-z]+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    .replace(/(href|src|xlink:href|formaction|poster)\s*=\s*(['"])\s*(javascript:|vbscript:|data:text\/html)/gi, '$1=$2#')
    .replace(/style\s*=\s*(['"])[\s\S]*?(expression\s*\(|javascript:|vbscript:|data:text\/html)[\s\S]*?\1/gi, '')
}

export function normalizeSafeWebUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const trimmed = url.trim()
  if (!/^https?:\/\//i.test(trimmed)) return ''
  if (/[\r\n\\]/.test(trimmed)) return ''
  return trimmed
}

export function normalizeMiniPagePath(path) {
  if (!path || typeof path !== 'string') return ''
  const trimmed = path.trim()
  if (!trimmed.startsWith('/pages/')) return ''
  if (trimmed.includes('..') || /[\r\n\\]/.test(trimmed)) return ''
  return trimmed
}
