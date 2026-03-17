export const makeXyConfig = (labels = [], overrides = {}) => {
  const normalizedLabels = Array.isArray(labels) ? labels : []
  const dense = normalizedLabels.length > 8
  const base = {
    responsive: true,
    useCssAnimation: true,
    chart: {
      backgroundColor: 'transparent',
      height: 220,
      padding: { top: 12, right: 14, bottom: 28, left: 44 },
      labels: {
        color: '#606266',
        xAxisLabels: {
          show: true,
          values: normalizedLabels,
          color: '#909399',
          fontSize: 11,
          showOnlyAtModulo: dense,
          modulo: dense ? 2 : 1
        },
        yAxis: {
          useNiceScale: true,
          gap: 12,
          labelWidth: 32,
          rounding: 0
        }
      }
    },
    legend: {
      show: false
    },
  }
  const merged = { ...base, ...overrides }
  if (overrides.chart) {
    merged.chart = { ...base.chart, ...overrides.chart }
    if (overrides.chart.labels) {
      merged.chart.labels = { ...base.chart.labels, ...overrides.chart.labels }
      if (overrides.chart.labels.xAxisLabels) {
        merged.chart.labels.xAxisLabels = {
          ...base.chart.labels.xAxisLabels,
          ...overrides.chart.labels.xAxisLabels
        }
      }
      if (overrides.chart.labels.yAxis) {
        merged.chart.labels.yAxis = {
          ...base.chart.labels.yAxis,
          ...overrides.chart.labels.yAxis
        }
      }
    }
  }
  return merged
}

export const donutConfig = {
  responsive: true,
  useCssAnimation: true,
  style: {
    chart: {
      backgroundColor: 'transparent',
      padding: { top: 8, right: 8, bottom: 8, left: 8 },
      layout: {
        labels: {
          dataLabels: {
            show: false
          }
        }
      }
    }
  },
  legend: {
    show: false
  }
}

export const makeMultiColorSeries = (labels, values, baseName, palette = []) => {
  const safeLabels = Array.isArray(labels) ? labels : []
  const safeValues = Array.isArray(values) ? values : []
  const colors = palette.length
    ? palette
    : ['#6366f1', '#22c55e', '#f59e0b', '#3b82f6', '#ef4444', '#14b8a6', '#a855f7']
  return safeLabels.map((label, index) => {
    const series = new Array(safeLabels.length).fill(null)
    series[index] = safeValues[index] ?? 0
    return {
      name: `${baseName}${label}`,
      type: 'bar',
      series,
      color: colors[index % colors.length]
    }
  })
}
