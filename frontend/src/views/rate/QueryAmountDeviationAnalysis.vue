
<template>
  <div class="deviation-analysis" style="width: 100%">
    <!-- 顶部筛选区 -->
    <a-card :bordered="false" class="filter-card">
      <div class="filter-content">
        <div class="filter-left">
          <span class="filter-label">统计日期：</span>
          <a-date-picker
            v-model="selectedDate"
            format="YYYY-MM-DD"
            placeholder="请选择日期"
            @change="onDateChange"            style="width: 200px; margin-right: 16px;"
          />
          <a-button type="primary" icon="search" @click="queryData">查询</a-button>
          <a-button icon="reload" @click="resetDate" style="margin-left: 8px;">重置</a-button>
        </div>
        <div class="filter-right">
          <a-tag color="blue">{{ analysisDate }}</a-tag>
        </div>
      </div>
    </a-card>

    <!-- 汇总统计卡片 -->
    <a-row :gutter="16" class="summary-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card planned-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="schedule" />
            </div>
            <div class="stat-info">
              <div class="stat-title">计划总金额</div>
              <div class="stat-value">¥{{ formatNumber(summary.totalPlannedAmount) }}</div>
              <div class="stat-desc">涉及商品 {{ summary.totalCommodities }} 种</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card actual-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="check-circle" />
            </div>
            <div class="stat-info">
              <div class="stat-title">实际总金额</div>
              <div class="stat-value">¥{{ formatNumber(summary.totalActualAmount) }}</div>
              <div class="stat-desc">达成率 {{ formatNumber(summary.overallDeviationRate) }}%</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card deviation-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="fall" />
            </div>
            <div class="stat-info">
              <div class="stat-title">总偏差金额</div>
              <div class="stat-value negative">¥{{ formatNumber(summary.totalDeviation) }}</div>
              <div class="stat-desc">平均偏差率 {{ formatNumber(summary.averageDeviationRate) }}%</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card status-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="warning" />
            </div>
            <div class="stat-info">
              <div class="stat-title">销售状态分布</div>
              <div class="status-tags">
                <a-tag color="red">偏低: {{ summary.lowSalesCount }}</a-tag>
                <a-tag color="green">正常: {{ summary.normalCount }}</a-tag>
                <a-tag color="blue">偏高: {{ summary.highSalesCount }}</a-tag>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" title="计划vs实际销售额对比">
          <apexchart
            type="bar"
            height="400"
            :options="comparisonChartOptions"
            :series="comparisonChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="偏差率分布">
          <apexchart
            type="donut"
            height="400"
            :options="deviationDistributionOptions"
            :series="deviationDistributionSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-row">
      <a-col :span="24">
        <a-card :bordered="false" title="各商品偏差率分析">
          <apexchart
            type="bar"
            height="450"
            :options="deviationRateChartOptions"
            :series="deviationRateChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据列表 -->
    <a-card :bordered="false" title="商品销售偏差明细" class="detail-list">
      <a-row :gutter="16">
        <a-col
          v-for="item in analysisList"
          :key="item.commodityId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="commodity-col"
        >
          <a-card :bordered="true" class="commodity-card" :class="'status-' + item.status.toLowerCase()">
            <div class="commodity-header">
              <div class="commodity-name">{{ item.commodityName }}</div>
              <a-tag :color="getStatusColor(item.status)">{{ item.statusDesc }}</a-tag>
            </div>
            <div class="commodity-body">
              <div class="info-row">
                <span class="info-label">商品编码:</span>
                <span class="info-value">{{ item.commodityCode }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">单价:</span>
                <span class="info-value">¥{{ formatNumber(item.unitPrice) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">计划数量:</span>
                <span class="info-value">{{ item.plannedQuantity }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实际数量:</span>
                <span class="info-value">{{ item.actualQuantity }}</span>
              </div>
              <div class="divider"></div>
              <div class="amount-section">
                <div class="amount-item">
                  <div class="amount-label">计划金额</div>
                  <div class="amount-value">¥{{ formatNumber(item.plannedAmount) }}</div>
                </div>
                <div class="amount-item">
                  <div class="amount-label">实际金额</div>
                  <div class="amount-value">¥{{ formatNumber(item.actualAmount) }}</div>
                </div>
              </div>
              <div class="deviation-section">
                <div class="deviation-item">
                  <span class="deviation-label">偏差金额:</span>
                  <span class="deviation-value negative">¥{{ formatNumber(item.amountDeviation) }}</span>
                </div>
                <div class="deviation-item">
                  <span class="deviation-label">偏差率:</span>
                  <span class="deviation-value negative">{{ formatNumber(item.deviationRate) }}%</span>
                </div>
              </div>
              <div class="reason-section">
                <a-icon type="info-circle" style="color: #faad14; margin-right: 4px;" />
                <span class="reason-text">{{ item.possibleReason }}</span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script>
import moment from 'moment'
moment.locale('zh-cn')
export default {
  name: 'QueryAmountDeviationAnalysis',
  data () {
    return {
      selectedDate: null,
      analysisDate: '',
      summary: {
        totalCommodities: 0,
        totalPlannedAmount: 0,
        totalActualAmount: 0,
        totalDeviation: 0,
        overallDeviationRate: 0,
        averageDeviationRate: 0,
        lowSalesCount: 0,
        highSalesCount: 0,
        normalCount: 0
      },
      analysisList: [],
      comparisonChartOptions: {},
      comparisonChartSeries: [],
      deviationDistributionOptions: {},
      deviationDistributionSeries: [],
      deviationRateChartOptions: {},
      deviationRateChartSeries: []
    }
  },
  mounted () {
    this.selectedDate = moment()
    this.analysisDate = this.selectedDate.format('YYYY-MM-DD')
    this.queryData()
  },
  methods: {
    moment,
    queryData () {
      var dateStr = this.analysisDate
      this.$get('/cos/statistics/queryAmountDeviationAnalysis', {
        date: dateStr
      }).then(function (r) {
        this.setData(r.data)
      }.bind(this))
    },

    setData (data) {
      this.analysisDate = data.date || this.analysisDate
      this.summary = data.summary || {}
      this.analysisList = data.analysisList || []

      this.initComparisonChart()
      this.initDeviationDistributionChart()
      this.initDeviationRateChart()
    },

    onDateChange (date, dateString) {
      if (date) {
        this.analysisDate = dateString
      }
    },

    resetDate () {
      this.selectedDate = this.$moment()
      this.analysisDate = this.selectedDate.format('YYYY-MM-DD')
      this.queryData()
    },

    initComparisonChart () {
      var list = this.analysisList || []
      var names = list.map(function (item) { return item.commodityName })
      var planned = list.map(function (item) { return item.plannedAmount })
      var actual = list.map(function (item) { return item.actualAmount })

      this.comparisonChartSeries = [
        {
          name: '计划金额',
          data: planned
        },
        {
          name: '实际金额',
          data: actual
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
          categories: names,
          labels: {
            rotate: -45,
            style: {
              fontSize: '11px'
            }
          }
        },
        yaxis: {
          title: {
            text: '金额 (元)'
          },
          labels: {
            formatter: function (val) { return val.toFixed(0) }
          }
        },
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter: function (value) { return '¥' + value.toFixed(2) }
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

    initDeviationDistributionChart () {
      var s = this.summary || {}
      this.deviationDistributionSeries = [
        s.lowSalesCount || 0,
        s.normalCount || 0,
        s.highSalesCount || 0
      ]

      this.deviationDistributionOptions = {
        chart: {
          type: 'donut'
        },
        labels: ['销售额偏低', '销售正常', '销售额偏高'],
        colors: ['#ff4d4f', '#52c41a', '#1890ff'],
        plotOptions: {
          pie: {
            donut: {
              size: '65%',
              labels: {
                show: true,
                total: {
                  show: true,
                  label: '商品总数',
                  formatter: function () {
                    return (s.totalCommodities || 0) + ' 种'
                  }
                }
              }
            }
          }
        },
        dataLabels: {
          enabled: true,
          formatter: function (val, opts) {
            return opts.w.globals.series[opts.seriesIndex] + ' 种'
          }
        },
        legend: {
          position: 'bottom'
        }
      }
    },

    initDeviationRateChart () {
      var list = this.analysisList || []
      var names = list.map(function (item) { return item.commodityName })
      var rates = list.map(function (item) { return item.deviationRate })
      var colors = list.map(function (item) {
        if (item.deviationRate < -50) return '#ff4d4f'
        if (item.deviationRate < 0) return '#faad14'
        return '#52c41a'
      })

      this.deviationRateChartSeries = [
        {
          name: '偏差率',
          data: rates
        }
      ]

      this.deviationRateChartOptions = {
        chart: {
          type: 'bar',
          toolbar: {
            show: true
          }
        },
        plotOptions: {
          bar: {
            horizontal: true,
            barHeight: '60%'
          }
        },
        colors: colors,
        dataLabels: {
          enabled: true,
          formatter: function (val) { return val.toFixed(2) + '%' }
        },
        xaxis: {
          title: {
            text: '偏差率 (%)'
          },
          labels: {
            formatter: function (val) { return val.toFixed(0) + '%' }
          }
        },
        yaxis: {
          categories: names,
          labels: {
            style: {
              fontSize: '12px'
            }
          }
        },
        tooltip: {
          x: {
            formatter: function (value, opts) {
              return names[opts.dataPointIndex]
            }
          },
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

    formatNumber (num) {
      if (num === null || num === undefined) return '0.00'
      return Number(num).toFixed(2)
    },

    getStatusColor (status) {
      if (status === 'LOW_SALES') return 'red'
      if (status === 'NORMAL') return 'green'
      if (status === 'HIGH_SALES') return 'blue'
      return 'default'
    }
  }
}
</script>

<style lang="less" scoped>.deviation-analysis {
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

      &.planned-card .stat-icon {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      }

      &.actual-card .stat-icon {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      }

      &.deviation-card .stat-icon {
        background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
      }

      &.status-card .stat-icon {
        background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
      }

      .stat-content {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 2px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 28px;
          margin-right: 16px;
          flex-shrink: 0;
          color: #fff;
        }

        .stat-info {
          flex: 1;

          .stat-title {
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
          }

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            line-height: 1;
            margin-bottom: 6px;
            color: #262626;

            &.negative {
              color: #ff4d4f;
            }
          }

          .stat-desc {
            font-size: 12px;
            color: #999;
          }

          .status-tags {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;

            .ant-tag {
              margin: 0;
            }
          }
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 16px;
  }

  .detail-list {
    border-radius: 2px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .commodity-col {
      margin-bottom: 16px;

      .commodity-card {
        border-radius: 2px;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          transform: translateY(-2px);
        }

        &.status-low_sales {
          border-left: 4px solid #ff4d4f;
        }

        &.status-normal {
          border-left: 4px solid #52c41a;
        }

        &.status-high_sales {
          border-left: 4px solid #1890ff;
        }

        .commodity-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          padding-bottom: 12px;
          border-bottom: 1px solid #f0f0f0;

          .commodity-name {
            font-size: 15px;
            font-weight: 600;
            color: #262626;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 70%;
          }
        }

        .commodity-body {
          .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 13px;

            .info-label {
              color: #999;
            }

            .info-value {
              color: #333;
              font-weight: 500;
            }
          }

          .divider {
            height: 1px;
            background: #f0f0f0;
            margin: 12px 0;
          }

          .amount-section {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;

            .amount-item {
              flex: 1;
              text-align: center;

              .amount-label {
                font-size: 12px;
                color: #999;
                margin-bottom: 4px;
              }

              .amount-value {
                font-size: 16px;
                font-weight: 600;
                color: #1890ff;
              }
            }
          }

          .deviation-section {
            background: #fff1f0;
            padding: 10px;
            border-radius: 2px;
            margin-bottom: 12px;

            .deviation-item {
              display: flex;
              justify-content: space-between;
              margin-bottom: 6px;
              font-size: 13px;

              &:last-child {
                margin-bottom: 0;
              }

              .deviation-label {
                color: #666;
              }

              .deviation-value {
                font-weight: 600;

                &.negative {
                  color: #ff4d4f;
                }
              }
            }
          }

          .reason-section {
            font-size: 12px;
            color: #666;
            background: #fffbe6;
            padding: 8px;
            border-radius: 2px;
            display: flex;
            align-items: flex-start;

            .reason-text {
              flex: 1;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }
}
</style>
