
<template>
  <div class="comparison-analysis" style="width: 100%">
    <!-- 顶部筛选区 -->
    <a-card :bordered="false" class="filter-card">
      <div class="filter-content">
        <div class="filter-left">
          <span class="filter-label">分析类型：</span>
          <a-radio-group v-model="analysisType" @change="onTypeChange" style="margin-right: 16px;">
            <a-radio value="week">周对比</a-radio>
            <a-radio value="month">月对比</a-radio>
          </a-radio-group>
          <a-button type="primary" icon="search" @click="queryData">查询</a-button>
          <a-button icon="reload" @click="resetAnalysis" style="margin-left: 8px;">重置</a-button>
        </div>
        <div class="filter-right">
          <a-tag color="blue">{{ currentMonthLabel }}</a-tag>
          <a-tag color="green">{{ lastMonthLabel }}</a-tag>
        </div>
      </div>
    </a-card>

    <!-- 核心指标对比卡片 -->
    <a-row :gutter="16" class="summary-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card quantity-card">
          <div class="stat-header">
            <div class="stat-title">实际销售数量</div>
            <a-icon type="shopping-cart" class="stat-icon" />
          </div>
          <div class="stat-body">
            <div class="current-value">
              <span class="value-label">本期:</span>
              <span class="value-number">{{ formatNumber(comparison.actualQuantity.currentValue) }}</span>
            </div>
            <div class="compare-value">
              <span class="value-label">上期:</span>
              <span class="value-number">{{ formatNumber(comparison.actualQuantity.lastValue) }}</span>
            </div>
            <div class="growth-info">
              <a-tag :color="getTrendColor(comparison.actualQuantity.trend)">
                <a-icon :type="getTrendIcon(comparison.actualQuantity.trend)" />
                {{ comparison.actualQuantity.trendDesc }}
              </a-tag>
              <span class="growth-rate">{{ formatGrowthRate(comparison.actualQuantity.growthRate) }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card amount-card">
          <div class="stat-header">
            <div class="stat-title">实际销售额</div>
            <a-icon type="yuan" class="stat-icon" />
          </div>
          <div class="stat-body">
            <div class="current-value">
              <span class="value-label">本期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.actualAmount.currentValue) }}</span>
            </div>
            <div class="compare-value">
              <span class="value-label">上期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.actualAmount.lastValue) }}</span>
            </div>
            <div class="growth-info">
              <a-tag :color="getTrendColor(comparison.actualAmount.trend)">
                <a-icon :type="getTrendIcon(comparison.actualAmount.trend)" />
                {{ comparison.actualAmount.trendDesc }}
              </a-tag>
              <span class="growth-rate">{{ formatGrowthRate(comparison.actualAmount.growthRate) }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card planned-card">
          <div class="stat-header">
            <div class="stat-title">计划金额</div>
            <a-icon type="schedule" class="stat-icon" />
          </div>
          <div class="stat-body">
            <div class="current-value">
              <span class="value-label">本期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.plannedAmount.currentValue) }}</span>
            </div>
            <div class="compare-value">
              <span class="value-label">上期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.plannedAmount.lastValue) }}</span>
            </div>
            <div class="growth-info">
              <a-tag :color="getTrendColor(comparison.plannedAmount.trend)">
                <a-icon :type="getTrendIcon(comparison.plannedAmount.trend)" />
                {{ comparison.plannedAmount.trendDesc }}
              </a-tag>
              <span class="growth-rate">{{ formatGrowthRate(comparison.plannedAmount.growthRate) }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card cost-card">
          <div class="stat-header">
            <div class="stat-title">原材料成本</div>
            <a-icon type="database" class="stat-icon" />
          </div>
          <div class="stat-body">
            <div class="current-value">
              <span class="value-label">本期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.materialCost.currentValue) }}</span>
            </div>
            <div class="compare-value">
              <span class="value-label">上期:</span>
              <span class="value-number">¥{{ formatNumber(comparison.materialCost.lastValue) }}</span>
            </div>
            <div class="growth-info">
              <a-tag :color="getTrendColor(comparison.materialCost.trend)">
                <a-icon :type="getTrendIcon(comparison.materialCost.trend)" />
                {{ comparison.materialCost.trendDesc }}
              </a-tag>
              <span class="growth-rate">{{ formatGrowthRate(comparison.materialCost.growthRate) }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 月度详细数据对比 -->
    <a-row :gutter="16" class="detail-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" :title="currentMonthLabel + ' 经营数据'" class="month-detail-card current-month">
          <div class="month-data">
            <div class="data-item">
              <div class="data-label">实际销售总量</div>
              <div class="data-value">{{ formatNumber(currentMonthData.totalActualQuantity) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">实际销售总额</div>
              <div class="data-value primary">¥{{ formatNumber(currentMonthData.totalActualAmount) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">计划总金额</div>
              <div class="data-value">¥{{ formatNumber(currentMonthData.totalPlannedAmount) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">原材料成本</div>
              <div class="data-value warning">¥{{ formatNumber(currentMonthData.totalMaterialCost) }}</div>
            </div>
            <div class="divider"></div>
            <div class="data-item highlight">
              <div class="data-label">毛利润</div>
              <div class="data-value" :class="currentMonthData.grossProfit >= 0 ? 'success' : 'negative'">
                ¥{{ formatNumber(currentMonthData.grossProfit) }}
              </div>
            </div>
            <div class="data-item highlight">
              <div class="data-label">毛利率</div>
              <div class="data-value" :class="currentMonthData.grossProfitMargin >= 0 ? 'success' : 'negative'">
                {{ formatNumber(currentMonthData.grossProfitMargin) }}%
              </div>
            </div>
            <div class="data-footer">
              <a-tag color="blue">商品数: {{ currentMonthData.commodityCount }}</a-tag>
              <a-tag color="green">记录数: {{ currentMonthData.recordCount }}</a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" :title="lastMonthLabel + ' 经营数据'" class="month-detail-card last-month">
          <div class="month-data">
            <div class="data-item">
              <div class="data-label">实际销售总量</div>
              <div class="data-value">{{ formatNumber(lastMonthData.totalActualQuantity) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">实际销售总额</div>
              <div class="data-value primary">¥{{ formatNumber(lastMonthData.totalActualAmount) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">计划总金额</div>
              <div class="data-value">¥{{ formatNumber(lastMonthData.totalPlannedAmount) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">原材料成本</div>
              <div class="data-value warning">¥{{ formatNumber(lastMonthData.totalMaterialCost) }}</div>
            </div>
            <div class="divider"></div>
            <div class="data-item highlight">
              <div class="data-label">毛利润</div>
              <div class="data-value" :class="lastMonthData.grossProfit >= 0 ? 'success' : 'negative'">
                ¥{{ formatNumber(lastMonthData.grossProfit) }}
              </div>
            </div>
            <div class="data-item highlight">
              <div class="data-label">毛利率</div>
              <div class="data-value" :class="lastMonthData.grossProfitMargin >= 0 ? 'success' : 'negative'">
                {{ formatNumber(lastMonthData.grossProfitMargin) }}%
              </div>
            </div>
            <div class="data-footer">
              <a-tag color="blue">商品数: {{ lastMonthData.commodityCount }}</a-tag>
              <a-tag color="green">记录数: {{ lastMonthData.recordCount }}</a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :span="24">
        <a-card :bordered="false" title="本期vs上期核心指标对比">
          <apexchart
            type="bar"
            height="400"
            :options="comparisonChartOptions"
            :series="comparisonChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" title="各项指标增长率">
          <apexchart
            type="bar"
            height="350"
            :options="growthRateChartOptions"
            :series="growthRateChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" title="本期指标占比分布">
          <apexchart
            type="pie"
            height="350"
            :options="distributionChartOptions"
            :series="distributionChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>export default {
  name: 'QueryAnalysis',
  data () {
    return {
      analysisType: 'month',
      currentMonthLabel: '',
      lastMonthLabel: '',
      comparison: {
        actualQuantity: {
          currentValue: 0,
          lastValue: 0,
          change: 0,
          growthRate: 0,
          trend: 'FLAT',
          trendDesc: '持平'
        },
        actualAmount: {
          currentValue: 0,
          lastValue: 0,
          change: 0,
          growthRate: 0,
          trend: 'FLAT',
          trendDesc: '持平'
        },
        plannedAmount: {
          currentValue: 0,
          lastValue: 0,
          change: 0,
          growthRate: 0,
          trend: 'FLAT',
          trendDesc: '持平'
        },
        materialCost: {
          currentValue: 0,
          lastValue: 0,
          change: 0,
          growthRate: 0,
          trend: 'FLAT',
          trendDesc: '持平'
        }
      },
      currentMonthData: {
        totalActualQuantity: 0,
        totalActualAmount: 0,
        totalPlannedAmount: 0,
        totalMaterialCost: 0,
        grossProfit: 0,
        grossProfitMargin: 0,
        commodityCount: 0,
        recordCount: 0
      },
      lastMonthData: {
        totalActualQuantity: 0,
        totalActualAmount: 0,
        totalPlannedAmount: 0,
        totalMaterialCost: 0,
        grossProfit: 0,
        grossProfitMargin: 0,
        commodityCount: 0,
        recordCount: 0
      },
      comparisonChartOptions: {},
      comparisonChartSeries: [],
      growthRateChartOptions: {},
      growthRateChartSeries: [],
      distributionChartOptions: {},
      distributionChartSeries: []
    }
  },
  mounted () {
    this.queryData()
  },
  methods: {
    queryData () {
      this.$get('/cos/statistics/queryAnalysis', {
        analysisType: this.analysisType
      }).then(function (r) {
        this.setData(r.data)
      }.bind(this))
    },

    setData (data) {
      this.comparison = data.comparison || {}
      this.currentMonthData = (data.currentMonth && data.currentMonth.data) || {}
      this.lastMonthData = (data.lastMonth && data.lastMonth.data) || {}
      this.currentMonthLabel = (data.currentMonth && data.currentMonth.month) || ''
      this.lastMonthLabel = (data.lastMonth && data.lastMonth.month) || ''
      this.analysisType = data.analysisType || 'month'

      this.initComparisonChart()
      this.initGrowthRateChart()
      this.initDistributionChart()
    },

    onTypeChange () {
      this.queryData()
    },

    resetAnalysis () {
      this.analysisType = 'month'
      this.queryData()
    },

    initComparisonChart () {
      var c = this.comparison || {}

      this.comparisonChartSeries = [
        {
          name: '本期',
          data: [
            c.actualQuantity ? c.actualQuantity.currentValue : 0,
            c.actualAmount ? c.actualAmount.currentValue : 0,
            c.plannedAmount ? c.plannedAmount.currentValue : 0,
            c.materialCost ? c.materialCost.currentValue : 0
          ]
        },
        {
          name: '上期',
          data: [
            c.actualQuantity ? c.actualQuantity.lastValue : 0,
            c.actualAmount ? c.actualAmount.lastValue : 0,
            c.plannedAmount ? c.plannedAmount.lastValue : 0,
            c.materialCost ? c.materialCost.lastValue : 0
          ]
        }
      ]

      this.comparisonChartOptions = {
        chart: {
          type: 'bar',
          toolbar: {
            show: true
          }
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '60%',
            endingShape: 'rounded'
          }
        },
        colors: ['#1890ff', '#52c41a'],
        dataLabels: {
          enabled: false
        },
        stroke: {
          show: true,
          width: 2,
          colors: ['transparent']
        },
        xaxis: {
          categories: ['销售数量', '销售额', '计划金额', '原材料成本'],
          labels: {
            style: {
              fontSize: '13px',
              fontWeight: 500
            }
          }
        },
        yaxis: {
          title: {
            text: '数值'
          },
          labels: {
            formatter: function (val) { return val.toFixed(0) }
          }
        },
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter: function (value) { return value.toFixed(2) }
          }
        },
        legend: {
          position: 'top'
        },
        grid: {
          borderColor: '#f1f1f1'
        }
      }
    },

    initGrowthRateChart () {
      var c = this.comparison || {}
      var categories = ['销售数量', '销售额', '计划金额', '原材料成本']
      var rates = [
        c.actualQuantity ? c.actualQuantity.growthRate : 0,
        c.actualAmount ? c.actualAmount.growthRate : 0,
        c.plannedAmount ? c.plannedAmount.growthRate : 0,
        c.materialCost ? c.materialCost.growthRate : 0
      ]
      var colors = rates.map(function (rate) {
        if (rate > 0) return '#52c41a'
        if (rate < 0) return '#ff4d4f'
        return '#faad14'
      })

      this.growthRateChartSeries = [
        {
          name: '增长率',
          data: rates
        }
      ]

      this.growthRateChartOptions = {
        chart: {
          type: 'bar',
          toolbar: {
            show: false
          }
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '50%',
            endingShape: 'rounded'
          }
        },
        colors: colors,
        dataLabels: {
          enabled: true,
          formatter: function (val) { return val.toFixed(2) + '%' }
        },
        stroke: {
          show: true,
          width: 2,
          colors: ['transparent']
        },
        xaxis: {
          categories: categories,
          labels: {
            style: {
              fontSize: '12px'
            }
          }
        },
        yaxis: {
          title: {
            text: '增长率 (%)'
          },
          labels: {
            formatter: function (val) { return val.toFixed(0) + '%' }
          }
        },
        tooltip: {
          y: {
            formatter: function (value) { return value.toFixed(2) + '%' }
          }
        },
        legend: {
          show: false
        },
        grid: {
          borderColor: '#f1f1f1'
        }
      }
    },

    initDistributionChart () {
      var cm = this.currentMonthData || {}

      this.distributionChartSeries = [
        cm.totalActualAmount || 0,
        cm.totalMaterialCost || 0,
        (cm.totalPlannedAmount || 0) - (cm.totalActualAmount || 0)
      ]

      this.distributionChartOptions = {
        chart: {
          type: 'pie'
        },
        labels: ['实际销售额', '原材料成本', '未达成计划'],
        colors: ['#52c41a', '#faad14', '#d9d9d9'],
        dataLabels: {
          enabled: true,
          formatter: function (val, opts) {
            return '¥' + opts.w.globals.series[opts.seriesIndex].toFixed(2)
          }
        },
        legend: {
          position: 'bottom'
        },
        tooltip: {
          y: {
            formatter: function (value) { return '¥' + value.toFixed(2) }
          }
        }
      }
    },

    formatNumber (num) {
      if (num === null || num === undefined) return '0.00'
      return Number(num).toFixed(2)
    },

    formatGrowthRate (rate) {
      if (rate === null || rate === undefined) return '0.00%'
      var prefix = rate > 0 ? '+' : ''
      return prefix + Number(rate).toFixed(2) + '%'
    },

    getTrendColor (trend) {
      if (trend === 'UP') return 'green'
      if (trend === 'DOWN') return 'red'
      return 'default'
    },

    getTrendIcon (trend) {
      if (trend === 'UP') return 'arrow-up'
      if (trend === 'DOWN') return 'arrow-down'
      return 'minus'
    }
  }
}
</script>

<style lang="less" scoped>.comparison-analysis {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .filter-card {
    margin-bottom: 16px;
    border-radius: 2px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .filter-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .filter-left {
        display: flex;
        align-items: center;

        .filter-label {
          font-size: 14px;
          color: #333;
          margin-right: 12px;
          font-weight: 500;
        }
      }

      .filter-right {
        .ant-tag {
          font-size: 14px;
          padding: 4px 12px;
          margin-left: 8px;
        }
      }
    }
  }

  .summary-cards {
    margin-bottom: 16px;

    .stat-card {
      border-radius: 2px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }

      &.quantity-card {
        border-top: 3px solid #1890ff;
      }

      &.amount-card {
        border-top: 3px solid #52c41a;
      }

      &.planned-card {
        border-top: 3px solid #722ed1;
      }

      &.cost-card {
        border-top: 3px solid #fa8c16;
      }

      .stat-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
        margin-bottom: 12px;

        .stat-title {
          font-size: 14px;
          color: #666;
          font-weight: 500;
        }

        .stat-icon {
          font-size: 20px;
          color: #1890ff;
        }
      }

      .stat-body {
        .current-value,
        .compare-value {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .value-label {
            font-size: 13px;
            color: #999;
          }

          .value-number {
            font-size: 18px;
            font-weight: 600;
            color: #262626;
          }
        }

        .growth-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px dashed #f0f0f0;

          .growth-rate {
            font-size: 14px;
            font-weight: 600;
            color: #1890ff;
          }
        }
      }
    }
  }

  .detail-row {
    margin-bottom: 16px;

    .month-detail-card {
      border-radius: 2px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &.current-month {
        border-left: 4px solid #1890ff;
      }

      &.last-month {
        border-left: 4px solid #52c41a;
      }

      .month-data {
        padding: 8px 0;

        .data-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 10px 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

          &.highlight {
            background: #fafafa;
            padding: 12px;
            border-radius: 2px;
            margin: 8px 0;
          }

          .data-label {
            font-size: 13px;
            color: #666;
          }

          .data-value {
            font-size: 16px;
            font-weight: 600;
            color: #262626;

            &.primary {
              color: #1890ff;
            }

            &.success {
              color: #52c41a;
            }

            &.warning {
              color: #faad14;
            }

            &.negative {
              color: #ff4d4f;
            }
          }
        }

        .divider {
          height: 2px;
          background: linear-gradient(to right, #1890ff, #52c41a);
          margin: 12px 0;
        }

        .data-footer {
          display: flex;
          gap: 8px;
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px dashed #f0f0f0;

          .ant-tag {
            margin: 0;
          }
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 16px;
  }
}
</style>
