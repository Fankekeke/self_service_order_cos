
<template>
  <div class="supply-fulfillment-analysis">
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
        <a-card :bordered="false" class="stat-card commodity-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="appstore" />
            </div>
            <div class="stat-info">
              <div class="stat-title">商品总数</div>
              <div class="stat-value">{{ summary.totalCommodities }}</div>
              <div class="stat-desc">参与统计商品数</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card rate-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="percentage" />
            </div>
            <div class="stat-info">
              <div class="stat-title">平均达成率</div>
              <div class="stat-value">{{ formatPercentage(summary.averageFulfillmentRate) }}</div>
              <div class="stat-desc">整体供应达成情况</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card insufficient-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="warning" />
            </div>
            <div class="stat-info">
              <div class="stat-title">供应不足</div>
              <div class="stat-value warning">{{ summary.insufficientSupplyCount }}</div>
              <div class="stat-desc">未达标商品数量</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card status-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="check-circle" />
            </div>
            <div class="stat-info">
              <div class="stat-title">状态分布</div>
              <div class="status-tags">
                <a-tag color="green">正常: {{ summary.normalCount }}</a-tag>
                <a-tag color="red">不足: {{ summary.insufficientSupplyCount }}</a-tag>
                <a-tag color="blue">超供: {{ summary.overPlannedCount }}</a-tag>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" title="各商品供应达成率分析">
          <apexchart
            type="bar"
            height="450"
            :options="fulfillmentRateChartOptions"
            :series="fulfillmentRateChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="供应状态分布">
          <apexchart
            type="donut"
            height="450"
            :options="statusDistributionOptions"
            :series="statusDistributionSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-row">
      <a-col :span="24">
        <a-card :bordered="false" title="计划量vs实际供应量对比">
          <apexchart
            type="bar"
            height="400"
            :options="comparisonChartOptions"
            :series="comparisonChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据列表 -->
    <a-card :bordered="false" title="商品供应达成明细" class="detail-list">
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
              <div class="divider"></div>
              <div class="quantity-section">
                <div class="quantity-item">
                  <div class="quantity-label">计划数量</div>
                  <div class="quantity-value">{{ formatNumber(item.plannedQuantity) }}</div>
                </div>
                <div class="quantity-item">
                  <div class="quantity-label">实际供应</div>
                  <div class="quantity-value primary">{{ formatNumber(item.actualQuantity) }}</div>
                </div>
              </div>
              <div class="fulfillment-section">
                <div class="fulfillment-label">达成率</div>
                <div class="fulfillment-value" :class="getRateClass(item.fulfillmentRate)">
                  {{ formatPercentage(item.fulfillmentRate) }}
                </div>
                <a-progress
                  :percent="Math.min(item.fulfillmentRate * 100, 100)"
                  :stroke-color="getProgressColor(item.fulfillmentRate)"
                  :status="getProgressStatus(item.fulfillmentRate)"
                  :show-info="false"
                  size="small"
                />
              </div>
              <div class="gap-section">
                <div class="gap-item" v-if="item.shortageQuantity > 0">
                  <a-icon type="arrow-down" style="color: #ff4d4f; margin-right: 4px;" />
                  <span class="gap-label">短缺:</span>
                  <span class="gap-value danger">{{ formatNumber(item.shortageQuantity) }}</span>
                </div>
                <div class="gap-item" v-if="item.surplusQuantity > 0">
                  <a-icon type="arrow-up" style="color: #52c41a; margin-right: 4px;" />
                  <span class="gap-label"> surplus:</span>
                  <span class="gap-value success">{{ formatNumber(item.surplusQuantity) }}</span>
                </div>
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
  name: 'SupplyFulfillmentRateAnalysis',
  data () {
    return {
      selectedDate: null,
      analysisDate: '',
      summary: {
        totalCommodities: 0,
        averageFulfillmentRate: 0,
        insufficientSupplyCount: 0,
        overPlannedCount: 0,
        normalCount: 0
      },
      analysisList: [],
      fulfillmentRateChartOptions: {},
      fulfillmentRateChartSeries: [],
      statusDistributionOptions: {},
      statusDistributionSeries: [],
      comparisonChartOptions: {},
      comparisonChartSeries: []
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
      this.$get('/cos/statistics/supplyFulfillmentRateAnalysis', {
        date: dateStr
      }).then(function (r) {
        this.setData(r.data)
      }.bind(this))
    },

    setData (data) {
      this.analysisDate = data.date || this.analysisDate
      this.summary = data.summary || {}
      this.analysisList = data.analysisList || []

      this.initFulfillmentRateChart()
      this.initStatusDistributionChart()
      this.initComparisonChart()
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

    initFulfillmentRateChart () {
      var list = this.analysisList || []
      var names = list.map(function (item) { return item.commodityName })
      var rates = list.map(function (item) { return item.fulfillmentRate * 100 })
      var colors = list.map(function (item) {
        if (item.status === 'INSUFFICIENT') return '#ff4d4f'
        if (item.status === 'OVER_PLANNED') return '#1890ff'
        return '#52c41a'
      })

      this.fulfillmentRateChartSeries = [
        {
          name: '达成率',
          data: rates
        }
      ]

      this.fulfillmentRateChartOptions = {
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
            text: '达成率 (%)'
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

    initStatusDistributionChart () {
      var s = this.summary || {}
      this.statusDistributionSeries = [
        s.normalCount || 0,
        s.insufficientSupplyCount || 0,
        s.overPlannedCount || 0
      ]

      this.statusDistributionOptions = {
        chart: {
          type: 'donut'
        },
        labels: ['正常', '供应不足', '超额供应'],
        colors: ['#52c41a', '#ff4d4f', '#1890ff'],
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

    initComparisonChart () {
      var list = this.analysisList || []
      var names = list.map(function (item) { return item.commodityName })
      var planned = list.map(function (item) { return item.plannedQuantity })
      var actual = list.map(function (item) { return item.actualQuantity })

      this.comparisonChartSeries = [
        {
          name: '计划数量',
          data: planned
        },
        {
          name: '实际供应',
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
            text: '数量'
          },
          labels: {
            formatter: function (val) { return val.toFixed(0) }
          }
        },
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter: function (value) { return value.toFixed(0) }
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

    formatNumber (num) {
      if (num === null || num === undefined) return '0.00'
      return Number(num).toFixed(2)
    },

    formatPercentage (rate) {
      if (rate === null || rate === undefined) return '0.00%'
      return (Number(rate) * 100).toFixed(2) + '%'
    },

    getStatusColor (status) {
      if (status === 'INSUFFICIENT') return 'red'
      if (status === 'OVER_PLANNED') return 'blue'
      if (status === 'NORMAL') return 'green'
      return 'default'
    },

    getRateClass (rate) {
      if (rate >= 1.0) return 'success'
      if (rate >= 0.8) return 'warning'
      return 'danger'
    },

    getProgressColor (rate) {
      if (rate >= 1.0) return '#52c41a'
      if (rate >= 0.8) return '#faad14'
      return '#ff4d4f'
    },

    getProgressStatus (rate) {
      if (rate >= 1.0) return 'success'
      if (rate >= 0.8) return 'normal'
      return 'exception'
    }
  }
}
</script>

<style lang="less" scoped>.supply-fulfillment-analysis {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .filter-card {
    margin-bottom: 16px;
    border-radius: 8px;
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
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }

      &.commodity-card .stat-icon {
        background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      }

      &.rate-card .stat-icon {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      }

      &.insufficient-card .stat-icon {
        background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
      }

      &.status-card .stat-icon {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      }

      .stat-content {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
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

            &.warning {
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
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .commodity-col {
      margin-bottom: 16px;

      .commodity-card {
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          transform: translateY(-2px);
        }

        &.status-normal {
          border-left: 4px solid #52c41a;
        }

        &.status-insufficient {
          border-left: 4px solid #ff4d4f;
        }

        &.status-over_planned {
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

          .quantity-section {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;

            .quantity-item {
              flex: 1;
              text-align: center;

              .quantity-label {
                font-size: 12px;
                color: #999;
                margin-bottom: 4px;
              }

              .quantity-value {
                font-size: 16px;
                font-weight: 600;
                color: #262626;

                &.primary {
                  color: #1890ff;
                }
              }
            }
          }

          .fulfillment-section {
            background: #fafafa;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 12px;

            .fulfillment-label {
              font-size: 12px;
              color: #999;
              margin-bottom: 6px;
            }

            .fulfillment-value {
              font-size: 20px;
              font-weight: 600;
              margin-bottom: 8px;

              &.success {
                color: #52c41a;
              }

              &.warning {
                color: #faad14;
              }

              &.danger {
                color: #ff4d4f;
              }
            }
          }

          .gap-section {
            .gap-item {
              display: flex;
              align-items: center;
              font-size: 13px;
              margin-bottom: 6px;

              &:last-child {
                margin-bottom: 0;
              }

              .gap-label {
                color: #666;
                margin-right: 4px;
              }

              .gap-value {
                font-weight: 600;

                &.danger {
                  color: #ff4d4f;
                }

                &.success {
                  color: #52c41a;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
