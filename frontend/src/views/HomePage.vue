
<template>
  <div class="home-page">
    <!-- 顶部标题卡片 -->
    <a-card :bordered="false" class="header-card">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">经营数据看板</h2>
          <p class="page-subtitle">实时掌握店铺运营状况与业务趋势</p>
        </div>
        <div class="header-right">
          <a-avatar :size="64" :src="shopInfo.avatar ? getImageUrl(shopInfo.avatar) : '/static/avatar/default.jpg'" />
          <span class="shop-name">{{ shopInfo.userName || '未设置' }}</span>
        </div>
      </div>
    </a-card>

    <!-- 核心指标卡片 -->
    <a-row :gutter="16" class="summary-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card revenue-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="yuan" />
            </div>
            <div class="stat-info">
              <div class="stat-title">今日营收</div>
              <div class="stat-value">¥{{ formatNumber(data.todayRevenue) }}</div>
              <div class="stat-trend">
                <span class="trend-label">本月:</span>
                <span class="trend-value">¥{{ formatNumber(data.monthRevenue) }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card order-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="shopping-cart" />
            </div>
            <div class="stat-info">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">{{ data.todayOrderNum }}</div>
              <div class="stat-trend">
                <span class="trend-label">本月:</span>
                <span class="trend-value">{{ data.monthOrderNum }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card user-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="user" />
            </div>
            <div class="stat-info">
              <div class="stat-title">总用户数</div>
              <div class="stat-value">{{ data.totalUserNum }}</div>
              <div class="stat-trend">
                <span class="trend-label">待审核:</span>
                <span class="trend-value">{{ data.auditUserNum }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card commodity-card">
          <div class="stat-content">
            <div class="stat-icon">
              <a-icon type="appstore" />
            </div>
            <div class="stat-info">
              <div class="stat-title">商品总数</div>
              <div class="stat-value">{{ data.totalCommodityNum }}</div>
              <div class="stat-trend">
                <span class="trend-label">总订单:</span>
                <span class="trend-value">{{ data.totalOrderNum }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 10天汇总统计 -->
    <a-row :gutter="16" class="ten-days-summary">
      <a-col :span="6">
        <a-card :bordered="false" class="summary-stat-card">
          <div class="summary-stat-content">
            <div class="summary-stat-icon">
              <a-icon type="shopping" />
            </div>
            <div class="summary-stat-info">
              <div class="summary-stat-label">10天总订单</div>
              <div class="summary-stat-value">{{ getTenDaysSummaryValue('totalOrderNum') }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="summary-stat-card">
          <div class="summary-stat-content">
            <div class="summary-stat-icon">
              <a-icon type="line-chart" />
            </div>
            <div class="summary-stat-info">
              <div class="summary-stat-label">10天总营收</div>
              <div class="summary-stat-value">¥{{ formatNumber(getTenDaysSummaryValue('totalRevenue')) }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="summary-stat-card">
          <div class="summary-stat-content">
            <div class="summary-stat-icon">
              <a-icon type="calendar" />
            </div>
            <div class="summary-stat-info">
              <div class="summary-stat-label">日均订单</div>
              <div class="summary-stat-value">{{ getTenDaysSummaryValue('avgDailyOrderNum') }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="summary-stat-card">
          <div class="summary-stat-content">
            <div class="summary-stat-icon">
              <a-icon type="rise" />
            </div>
            <div class="summary-stat-info">
              <div class="summary-stat-label">日均营收</div>
              <div class="summary-stat-value">¥{{ formatNumber(getTenDaysSummaryValue('avgDailyRevenue')) }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 订单状态统计 -->

    <!-- 图表区域 - 第一行 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" title="近7天订单与营收趋势">
          <apexchart
            type="area"
            height="350"
            :options="orderRevenueChartOptions"
            :series="orderRevenueChartSeries"
          ></apexchart>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="热门商品TOP7">
          <div class="top-commodities-list">
            <div
              v-for="(item, index) in data.topCommodityStats"
              :key="item.commodityId"
              class="commodity-item"
            >
              <div class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <img
                v-if="item.commodityImages"
                :src="getImageUrl(item.commodityImages)"
                :alt="item.commodityName"
                class="commodity-thumb"
              />
              <div class="commodity-detail">
                <div class="commodity-name">{{ item.commodityName }}</div>
                <div class="commodity-meta">
                  <span class="order-count">销量: {{ item.orderCount }}</span>
                  <span class="revenue">¥{{ formatNumber(item.revenue) }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 - 第二行 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" title="热门商品销量统计">
          <apexchart
            type="bar"
            height="350"
            :options="topCommodityStatsOptions"
            :series="topCommodityStatsSeries"
          ></apexchart>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" title="近10天订单详情">
          <apexchart
            type="line"
            height="350"
            :options="lastTenDaysOptions"
            :series="lastTenDaysSeries"
          ></apexchart>
        </a-card>
      </a-col>
    </a-row>

    <!-- 月度汇总与店铺统计 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" title="本月经营汇总">
          <div class="month-summary">
            <div class="summary-item">
              <div class="summary-label">订单数量</div>
              <div class="summary-value primary">{{ getMonthOrderNum() }}</div>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-item">
              <div class="summary-label">订单金额</div>
              <div class="summary-value success">¥{{ formatNumber(getMonthOrderPrice()) }}</div>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-item">
              <div class="summary-label">客单价</div>
              <div class="summary-value warning">
                ¥{{ calculateAvgOrderValue(getMonthOrderPrice(), getMonthOrderNum()) }}
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表格 -->
    <a-card :bordered="false" title="近期订单营收明细" class="detail-table">
      <a-table
        :columns="columns"
        :data-source="data.orderRevenueData || []"
        :pagination="false"
        row-key="days"
        size="middle"
      >
        <template slot="date" slot-scope="text">
          <a-icon type="calendar" style="margin-right: 8px; color: #1890ff;" />
          {{ text }}
        </template>
        <template slot="count" slot-scope="text">
          <a-badge :count="text" :number-style="{ backgroundColor: '#52c41a' }" />
        </template>
        <template slot="orderPrice" slot-scope="text">
          <span class="price-text">¥{{ formatNumber(text) }}</span>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>export default {
  name: 'HomePage',
  data () {
    return {
      data: {
        todayRevenue: 0,
        monthRevenue: 0,
        yearRevenue: 0,
        totalRevenue: 0,
        todayOrderNum: 0,
        monthOrderNum: 0,
        yearOrderNum: 0,
        totalOrderNum: 0,
        totalUserNum: 0,
        auditUserNum: 0,
        totalCommodityNum: 0,
        topCommodityStats: [],
        orderRevenueData: [],
        lastTenDaysStats: [],
        tenDaysSummary: {},
        orderStatusStats: {},
        orderMonthRevenue: {},
        shopList: [],
        shopOrderRate: []
      },
      shopInfo: {},
      columns: [
        {
          title: '日期',
          dataIndex: 'days',
          key: 'days',
          scopedSlots: { customRender: 'date' },
          width: 120
        },
        {
          title: '订单数量',
          dataIndex: 'count',
          key: 'count',
          scopedSlots: { customRender: 'count' },
          width: 150,
          sorter: function (a, b) { return a.count - b.count }
        },
        {
          title: '订单金额',
          dataIndex: 'orderPrice',
          key: 'orderPrice',
          scopedSlots: { customRender: 'orderPrice' },
          sorter: function (a, b) { return a.orderPrice - b.orderPrice }
        },
        {
          title: '客单价',
          key: 'avgPrice',
          width: 150,
          customRender: function (text, record) {
            var avg = record.count > 0 ? (record.orderPrice / record.count).toFixed(2) : '0.00'
            return '¥' + avg
          }
        }
      ],
      orderRevenueChartOptions: {},
      orderRevenueChartSeries: [],
      topCommodityStatsOptions: {},
      topCommodityStatsSeries: [],
      lastTenDaysOptions: {},
      lastTenDaysSeries: [],
      shopOrderRateOptions: {},
      shopOrderRateSeries: []
    }
  },
  mounted () {
    this.fetchData()
  },
  methods: {
    fetchData () {
      this.$get('/cos/order-info/home').then(function (response) {
        this.setData(response.data)
      }.bind(this))
    },

    setData (data) {
      this.data = data
      this.shopInfo = data.shopList && data.shopList.length > 0 ? data.shopList[0] : {}

      this.initOrderRevenueChart()
      this.initTopCommodityStatsChart()
      this.initLastTenDaysChart()
      this.initShopOrderRateChart()
    },

    initOrderRevenueChart () {
      var orderData = this.data.orderRevenueData || []
      var dates = orderData.map(function (item) { return item.days })

      this.orderRevenueChartSeries = [
        {
          name: '订单数量',
          data: orderData.map(function (item) { return item.count })
        },
        {
          name: '营收金额',
          data: orderData.map(function (item) { return item.orderPrice })
        }
      ]

      this.orderRevenueChartOptions = {
        chart: {
          type: 'area',
          toolbar: {
            show: true
          },
          animations: {
            enabled: true
          }
        },
        colors: ['#1890ff', '#52c41a'],
        stroke: {
          curve: 'smooth',
          width: 2
        },
        fill: {
          type: 'gradient',
          gradient: {
            shadeIntensity: 1,
            opacityFrom: 0.7,
            opacityTo: 0.3,
            stops: [0, 90, 100]
          }
        },
        dataLabels: {
          enabled: false
        },
        xaxis: {
          categories: dates,
          labels: {
            style: {
              fontSize: '12px'
            }
          }
        },
        yaxis: [
          {
            title: {
              text: '订单数量',
              style: {
                fontSize: '12px'
              }
            },
            labels: {
              formatter: function (val) { return Math.round(val) }
            }
          },
          {
            opposite: true,
            title: {
              text: '营收金额 (元)',
              style: {
                fontSize: '12px'
              }
            },
            labels: {
              formatter: function (val) { return val.toFixed(0) }
            }
          }
        ],
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter: function (value, opts) {
              if (opts.seriesIndex === 0) {
                return value + ' 单'
              }
              return '¥' + value.toFixed(2)
            }
          }
        },
        legend: {
          position: 'top',
          horizontalAlign: 'right'
        },
        grid: {
          borderColor: '#f1f1f1'
        }
      }
    },

    initTopCommodityStatsChart () {
      var commodities = this.data.topCommodityStats || []
      var names = commodities.map(function (item) { return item.commodityName })
      var counts = commodities.map(function (item) { return item.orderCount })
      var revenues = commodities.map(function (item) { return item.revenue })

      this.topCommodityStatsSeries = [
        {
          name: '订单数量',
          data: counts
        },
        {
          name: '营收金额',
          data: revenues
        }
      ]

      this.topCommodityStatsOptions = {
        chart: {
          type: 'bar',
          toolbar: {
            show: false
          }
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '60%',
            endingShape: 'rounded'
          }
        },
        colors: ['#722ed1', '#faad14'],
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
        yaxis: [
          {
            title: {
              text: '订单数量'
            }
          },
          {
            opposite: true,
            title: {
              text: '营收金额 (元)'
            }
          }
        ],
        tooltip: {
          shared: true,
          intersect: false
        },
        legend: {
          position: 'top'
        },
        grid: {
          borderColor: '#f1f1f1'
        }
      }
    },

    initLastTenDaysChart () {
      var statsData = this.data.lastTenDaysStats || []
      var dates = statsData.map(function (item) { return item.date })

      this.lastTenDaysSeries = [
        {
          name: '订单数',
          data: statsData.map(function (item) { return item.orderNum })
        },
        {
          name: '营收',
          data: statsData.map(function (item) { return item.revenue })
        },
        {
          name: '菜品数',
          data: statsData.map(function (item) { return item.mealCount })
        }
      ]

      this.lastTenDaysOptions = {
        chart: {
          type: 'line',
          toolbar: {
            show: true
          }
        },
        colors: ['#1890ff', '#52c41a', '#fa8c16'],
        stroke: {
          curve: 'smooth',
          width: 3
        },
        markers: {
          size: 5
        },
        xaxis: {
          categories: dates,
          labels: {
            style: {
              fontSize: '12px'
            }
          }
        },
        yaxis: [
          {
            title: {
              text: '订单数 / 菜品数'
            }
          },
          {
            opposite: true,
            title: {
              text: '营收 (元)'
            },
            labels: {
              formatter: function (val) { return val.toFixed(0) }
            }
          }
        ],
        tooltip: {
          shared: true,
          intersect: false
        },
        legend: {
          position: 'top'
        },
        grid: {
          borderColor: '#f1f1f1'
        }
      }
    },

    initShopOrderRateChart () {
      var shopData = this.data.shopOrderRate || []

      this.shopOrderRateSeries = shopData.map(function (item) { return item.orderNum })

      var labels = shopData.map(function (item, index) {
        return item.userName || ('店铺' + (index + 1))
      })

      this.shopOrderRateOptions = {
        chart: {
          type: 'pie',
          animations: {
            enabled: true
          }
        },
        labels: labels,
        colors: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1'],
        dataLabels: {
          enabled: true,
          formatter: function (val, opts) {
            var count = opts.w.globals.series[opts.seriesIndex]
            return count + ' 单'
          }
        },
        legend: {
          position: 'bottom'
        },
        tooltip: {
          y: {
            formatter: function (value) { return value + ' 单' }
          }
        }
      }
    },

    formatNumber: function (num) {
      if (num === null || num === undefined) return '0.00'
      return Number(num).toFixed(2)
    },

    calculateAvgOrderValue: function (revenue, orderNum) {
      if (!orderNum || orderNum === 0) return '0.00'
      return (revenue / orderNum).toFixed(2)
    },

    getImageUrl: function (imageName) {
      if (!imageName) return ''
      return 'http://127.0.0.1:9527/imagesWeb/' + imageName
    },

    getOrderStatusValue: function (key) {
      if (!this.data.orderStatusStats) return key === 'completionRate' ? '0.00' : 0
      return this.data.orderStatusStats[key] || (key === 'completionRate' ? '0.00' : 0)
    },

    getMonthOrderNum: function () {
      if (!this.data.orderMonthRevenue) return 0
      return this.data.orderMonthRevenue.orderNum || 0
    },

    getMonthOrderPrice: function () {
      if (!this.data.orderMonthRevenue) return 0
      return this.data.orderMonthRevenue.orderPrice || 0
    },

    getTenDaysSummaryValue: function (key) {
      if (!this.data.tenDaysSummary) {
        if (key === 'avgDailyOrderNum' || key === 'avgDailyRevenue') return '0.00'
        return 0
      }
      var value = this.data.tenDaysSummary[key]
      if (value === null || value === undefined) {
        if (key === 'avgDailyOrderNum' || key === 'avgDailyRevenue') return '0.00'
        return 0
      }
      return value
    }
  }
}
</script>

<style lang="less" scoped>.home-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .header-card {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #2ca7ff 0%, #1a469d 100%);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        .page-title {
          color: #fff;
          font-size: 28px;
          font-weight: 600;
          margin: 0 0 8px 0;
        }

        .page-subtitle {
          color: rgba(255, 255, 255, 0.85);
          font-size: 14px;
          margin: 0;
        }
      }

      .header-right {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .shop-name {
          color: #fff;
          font-size: 14px;
          font-weight: 500;
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

      &.revenue-card .stat-icon {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      }

      &.order-card .stat-icon {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      }

      &.user-card .stat-icon {
        background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      }

      &.commodity-card .stat-icon {
        background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
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
            font-size: 28px;
            font-weight: 600;
            line-height: 1;
            margin-bottom: 8px;
            color: #262626;
          }

          .stat-trend {
            font-size: 12px;
            color: #999;

            .trend-label {
              margin-right: 4px;
            }

            .trend-value {
              color: #1890ff;
              font-weight: 500;
            }
          }
        }
      }
    }
  }

  .ten-days-summary {
    margin-bottom: 16px;

    .summary-stat-card {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }

      .summary-stat-content {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .summary-stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 22px;
          margin-right: 12px;
          flex-shrink: 0;
          color: #fff;
          background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
        }

        .summary-stat-info {
          flex: 1;

          .summary-stat-label {
            font-size: 13px;
            color: #666;
            margin-bottom: 6px;
          }

          .summary-stat-value {
            font-size: 22px;
            font-weight: 600;
            color: #262626;
          }
        }
      }
    }
  }

  .status-row {
    margin-bottom: 16px;

    .status-card {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }

      &.pending-card .status-icon {
        background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
      }

      &.completed-card .status-icon {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      }

      &.rate-card .status-icon {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      }

      .status-content {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .status-icon {
          width: 50px;
          height: 50px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          margin-right: 12px;
          flex-shrink: 0;
          color: #fff;
        }

        .status-info {
          flex: 1;

          .status-label {
            font-size: 13px;
            color: #666;
            margin-bottom: 4px;
          }

          .status-value {
            font-size: 24px;
            font-weight: 600;
            color: #262626;
          }
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 16px;
  }

  .top-commodities-list {
    max-height: 350px;
    overflow-y: auto;

    .commodity-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      transition: all 0.3s;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background: #fafafa;
        padding-left: 8px;
      }

      .rank-badge {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: 600;
        margin-right: 12px;
        flex-shrink: 0;
        color: #fff;

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
          color: #333;
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
          color: #333;
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
        }

        &.rank-4, &.rank-5, &.rank-6, &.rank-7 {
          background: #d9d9d9;
          color: #666;
        }
      }

      .commodity-thumb {
        width: 50px;
        height: 50px;
        object-fit: cover;
        border-radius: 6px;
        margin-right: 12px;
        flex-shrink: 0;
      }

      .commodity-detail {
        flex: 1;
        min-width: 0;

        .commodity-name {
          font-size: 14px;
          font-weight: 500;
          color: #262626;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .commodity-meta {
          display: flex;
          justify-content: space-between;
          font-size: 12px;

          .order-count {
            color: #1890ff;
            font-weight: 500;
          }

          .revenue {
            color: #ff4d4f;
            font-weight: 600;
          }
        }
      }
    }
  }

  .month-summary {
    padding: 24px 0;

    .summary-item {
      text-align: center;
      padding: 16px 0;

      .summary-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 12px;
      }

      .summary-value {
        font-size: 32px;
        font-weight: 600;

        &.primary {
          color: #1890ff;
        }

        &.success {
          color: #52c41a;
        }

        &.warning {
          color: #faad14;
        }
      }
    }

    .summary-divider {
      height: 1px;
      background: #f0f0f0;
      margin: 0 40px;
    }
  }

  .detail-table {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .price-text {
      color: #52c41a;
      font-weight: 600;
    }
  }
}
</style>
