const mergeXyConfig = (base, overrides = {}) => {
  const merged = { ...base, ...overrides }

  if (base.chart || overrides.chart) {
    merged.chart = { ...(base.chart || {}), ...(overrides.chart || {}) }
  }

  if (base.chart?.userOptions || overrides.chart?.userOptions) {
    merged.chart.userOptions = {
      ...(base.chart?.userOptions || {}),
      ...(overrides.chart?.userOptions || {})
    }
  }

  if (base.chart?.userOptions?.buttons || overrides.chart?.userOptions?.buttons) {
    merged.chart.userOptions.buttons = {
      ...(base.chart?.userOptions?.buttons || {}),
      ...(overrides.chart?.userOptions?.buttons || {})
    }
  }

  if (base.chart?.zoom || overrides.chart?.zoom) {
    merged.chart.zoom = { ...(base.chart?.zoom || {}), ...(overrides.chart?.zoom || {}) }
  }

  if (base.chart?.zoom?.preview || overrides.chart?.zoom?.preview) {
    merged.chart.zoom.preview = {
      ...(base.chart?.zoom?.preview || {}),
      ...(overrides.chart?.zoom?.preview || {})
    }
  }

  if (base.chart?.grid || overrides.chart?.grid) {
    merged.chart.grid = { ...(base.chart?.grid || {}), ...(overrides.chart?.grid || {}) }
  }

  if (base.chart?.grid?.labels || overrides.chart?.grid?.labels) {
    merged.chart.grid.labels = {
      ...(base.chart?.grid?.labels || {}),
      ...(overrides.chart?.grid?.labels || {})
    }
  }

  if (base.chart?.grid?.labels?.xAxisLabels || overrides.chart?.grid?.labels?.xAxisLabels) {
    merged.chart.grid.labels.xAxisLabels = {
      ...(base.chart?.grid?.labels?.xAxisLabels || {}),
      ...(overrides.chart?.grid?.labels?.xAxisLabels || {})
    }
  }

  if (base.chart?.grid?.labels?.yAxis || overrides.chart?.grid?.labels?.yAxis) {
    merged.chart.grid.labels.yAxis = {
      ...(base.chart?.grid?.labels?.yAxis || {}),
      ...(overrides.chart?.grid?.labels?.yAxis || {})
    }
  }

  if (base.chart?.grid?.labels?.xAxis || overrides.chart?.grid?.labels?.xAxis) {
    merged.chart.grid.labels.xAxis = {
      ...(base.chart?.grid?.labels?.xAxis || {}),
      ...(overrides.chart?.grid?.labels?.xAxis || {})
    }
  }

  if (base.chart?.tooltip || overrides.chart?.tooltip) {
    merged.chart.tooltip = { ...(base.chart?.tooltip || {}), ...(overrides.chart?.tooltip || {}) }
  }

  if (base.chart?.legend || overrides.chart?.legend) {
    merged.chart.legend = { ...(base.chart?.legend || {}), ...(overrides.chart?.legend || {}) }
  }

  if (base.bar || overrides.bar) {
    merged.bar = { ...(base.bar || {}), ...(overrides.bar || {}) }
  }

  return merged
}

export const makeXyConfig = (labels = [], overrides = {}) => {
  const normalizedLabels = Array.isArray(labels) ? labels : []
  const dense = normalizedLabels.length > 8
  const extraDense = normalizedLabels.length > 16
  const sparseBar = normalizedLabels.length <= 2
  const fewBars = normalizedLabels.length > 2 && normalizedLabels.length <= 5

  const base = {
    responsive: true,
    useCssAnimation: false,
    bar: {
      borderRadius: 6,
      periodGap: sparseBar ? 0.38 : fewBars ? 0.24 : 0.12,
      innerGap: 0.08
    },
    chart: {
      fontFamily: 'inherit',
      backgroundColor: 'transparent',
      color: '#334155',
      height: 256,
      padding: { top: 18, right: 22, bottom: dense ? 42 : 32, left: 52 },
      userOptions: {
        show: false,
        showOnChartHover: false,
        keepStateOnChartLeave: false,
        buttons: {
          zoom: false,
          table: false,
          tooltip: false,
          labels: false,
          csv: false,
          img: false,
          pdf: false,
          svg: false,
          annotator: false,
          animation: false,
          fullscreen: false,
          stack: false,
          sort: false
        }
      },
      zoom: {
        show: false,
        enableRangeHandles: false,
        enableSelectionDrag: false,
        preview: {
          enable: false
        }
      },
      highlighter: {
        color: '#2563eb',
        opacity: 0,
        useLine: true,
        lineWidth: 1,
        lineDasharray: 4
      },
      grid: {
        stroke: 'rgba(148, 163, 184, 0.28)',
        showHorizontalLines: true,
        showVerticalLines: false,
        labels: {
          show: true,
          color: '#64748b',
          fontSize: 12,
          xAxis: {
            showBaseline: true,
            showCrosshairs: false
          },
          xAxisLabels: {
            show: true,
            values: normalizedLabels,
            color: '#94a3b8',
            fontSize: 11,
            rotation: dense ? -28 : 0,
            yOffset: dense ? 10 : 0,
            showOnlyAtModulo: dense,
            modulo: extraDense ? 3 : 2
          },
          yAxis: {
            commonScaleSteps: 4,
            useIndividualScale: false,
            useNiceScale: true,
            gap: 12,
            labelWidth: 36,
            rounding: 0,
            showBaseline: true
          }
        }
      },
      tooltip: {
        show: true,
        backgroundColor: '#fffbff',
        color: '#1f2937',
        borderColor: '#dbe5f0',
        borderWidth: 1,
        borderRadius: 12,
        fontSize: 12,
        backgroundOpacity: 100,
        smooth: false,
        backdropFilter: false,
        offsetY: 8
      },
      legend: {
        show: false,
        position: 'bottom',
        color: '#475569',
        fontSize: 12
      }
    }
  }

  return mergeXyConfig(base, overrides)
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

export const makeBarSeries = (name, values, color, extra = {}) => ([{
  name,
  type: 'bar',
  color,
  dataLabels: false,
  series: Array.isArray(values) ? values.map(value => value ?? 0) : [],
  ...extra
}])

export const makeLineSeries = (name, values, color, extra = {}) => ([{
  name,
  type: 'line',
  color,
  dataLabels: false,
  smooth: true,
  showSerieName: 'end',
  series: Array.isArray(values) ? values.map(value => value ?? 0) : [],
  ...extra
}])

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
