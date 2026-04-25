<template>
  <a-card :bordered="false" class="card-area">
    <div :class="advanced ? 'search' : null">
      <!-- 搜索区域 -->
      <a-form layout="horizontal">
        <a-row :gutter="15">
          <div :class="advanced ? null: 'fold'">
            <a-col :md="6" :sm="24">
              <a-form-item
                label="购买人"
                :labelCol="{span: 5}"
                :wrapperCol="{span: 18, offset: 1}">
                <a-input v-model="queryParams.userName"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item
                label="订单状态"
                :labelCol="{span: 5}"
                :wrapperCol="{span: 18, offset: 1}">
                <a-select v-model="queryParams.orderStatus" placeholder="请选择订单状态" allowClear>
                  <a-select-option value="0">排队中</a-select-option>
                  <a-select-option value="1">等待取餐</a-select-option>
                  <a-select-option value="2">已经取餐</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </div>
          <span style="float: right; margin-top: 3px;">
            <a-button type="primary" @click="search">查询</a-button>
            <a-button style="margin-left: 8px" @click="reset">重置</a-button>
          </span>
        </a-row>
      </a-form>
    </div>
    <div>
      <div class="operator">
        <!--        <a-button type="primary" ghost @click="add">新增</a-button>-->
        <a-button @click="batchDelete">删除</a-button>
      </div>
      <!-- 表格区域 -->
      <a-table ref="TableInfo"
               :columns="columns"
               :rowKey="record => record.id"
               :dataSource="dataSource"
               :pagination="pagination"
               :loading="loading"
               :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
               :scroll="{ x: 900 }"
               @change="handleTableChange">
        <div slot="expandedRowRender" slot-scope="record" style="margin: 0">
          <div v-if="record.orderDetails && record.orderDetails.length > 0" class="order-detail-container">
            <div class="detail-header">
              <a-icon type="shopping-cart" style="color: #1890ff; margin-right: 8px;" />
              <span>订单明细</span>
              <a-badge :count="record.orderDetails.length" style="margin-left: 8px;" />
            </div>
            <a-row :gutter="16">
              <a-col
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
                v-for="(detail, index) in record.orderDetails"
                :key="index">
                <a-card size="small" :bordered="true" hoverable class="detail-card" style="margin-bottom: 10px">
                  <div class="detail-content">
                    <div class="detail-image-wrapper">
                      <img
                        v-if="detail.commodityImages"
                        :src="getImageUrl(detail.commodityImages)"
                        :alt="detail.commodityName"
                        class="commodity-image"
                      />
                      <div v-else class="no-image">
                        <a-icon type="picture" style="font-size: 32px; color: #d9d9d9;" />
                      </div>
                    </div>
                    <div class="detail-info">
                      <div class="info-header">
                        <h4 class="commodity-name" :title="detail.commodityName">{{ detail.commodityName }}</h4>
                      </div>
                      <div class="commodity-desc" :title="detail.commodityContent">
                        {{ detail.commodityContent || '暂无描述' }}
                      </div>
                      <div class="price-quantity">
                        <div class="price-item">
                          <span class="label">单价：</span>
                          <span class="value price">¥{{ detail.itemPrice.toFixed(2) }}</span>
                        </div>
                        <div class="quantity-item">
                          <span class="label">数量：</span>
                          <span class="value quantity">{{ detail.quantity }}</span>
                        </div>
                      </div>
                      <div class="subtotal">
                        <span class="subtotal-label">小计：</span>
                        <span class="subtotal-value">¥{{ (detail.itemPrice * detail.quantity / 100).toFixed(2) }}</span>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
            </a-row>
            <div class="order-summary">
              <a-divider />
              <div class="summary-content">
                <div class="summary-item">
                  <span class="summary-label">商品总量：</span>
                  <span class="summary-value">{{ getTotalQuantity(record.orderDetails) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">商品种类：</span>
                  <span class="summary-value">{{ record.orderDetails.length }} 种</span>
                </div>
                <div class="summary-item total">
                  <span class="summary-label">订单总额：</span>
                  <span class="summary-value highlight">¥{{ record.orderPrice.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-data">
            <a-empty description="暂无订单明细" />
          </div>
        </div>
        <template slot="nameShow" slot-scope="text, record">
          <template>
            <a-tooltip>
              <template slot="title">
                {{ record.name }}
              </template>
              {{ record.name.slice(0, 10) }} ...
            </a-tooltip>
          </template>
        </template>
        <template slot="userNameShow" slot-scope="text, record">
          <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" :src="record.userImages" />
            </template>
            <a>{{ record.userName }}</a>
          </a-popover>
        </template>
        <template slot="addressShow" slot-scope="text, record">
          <template>
            <a-tooltip>
              <template slot="title">
                {{ record.address }}
              </template>
              {{ record.address.slice(0, 10) }} ...
            </a-tooltip>
          </template>
        </template>
        <template slot="operation" slot-scope="text, record">
          <a-icon
            v-if="record.orderStatus === '0'"
            type="check-circle"
            theme="twoTone"
            twoToneColor="#52c41a"
            @click="completeOrder(record)"
            title="出餐">
          </a-icon>
        </template>
      </a-table>
    </div>
    <order-view
      @close="handleOrderViewClose"
      @checkClose="handleOrderCheckClose"
      :orderShow="orderView.visiable"
      :orderData="orderView.data">
    </order-view>
  </a-card>
</template>

<script>
import RangeDate from '@/components/datetime/RangeDate'
import {mapState} from 'vuex'
import moment from 'moment'
import OrderView from './OrderView'
moment.locale('zh-cn')

export default {
  name: 'order',
  components: {OrderView, RangeDate},
  data () {
    return {
      orderView: {
        visiable: false,
        data: null
      },
      advanced: false,
      queryParams: {},
      filteredInfo: null,
      sortedInfo: null,
      paginationInfo: null,
      dataSource: [],
      selectedRowKeys: [],
      loading: false,
      pagination: {
        pageSizeOptions: ['10', '20', '30', '40', '100'],
        defaultCurrent: 1,
        defaultPageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total, range) => `显示 ${range[0]} ~ ${range[1]} 条记录，共 ${total} 条记录`
      },
      userList: []
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    columns () {
      return [{
        title: '订单编号',
        ellipsis: true,
        dataIndex: 'code'
      }, {
        title: '订单状态',
        ellipsis: true,
        dataIndex: 'orderStatus',
        customRender: (text, row, index) => {
          switch (text) {
            case '0':
              return <a-tag color="orange">排队中</a-tag>
            case '1':
              return <a-tag color="blue">等待取餐</a-tag>
            case '2':
              return <a-tag color="green">已经取餐</a-tag>
            default:
              return '- -'
          }
        }
      }, {
        title: '订单价格',
        ellipsis: true,
        dataIndex: 'orderPrice',
        customRender: (text, row, index) => {
          if (text !== null) {
            return '￥' + text
          } else {
            return '- -'
          }
        }
      }, {
        title: '商品总量',
        ellipsis: true,
        dataIndex: 'orderPrice',
        customRender: (text, row, index) => {
          if (text !== null) {
            return this.getTotalQuantity(row.orderDetails) + '克'
          } else {
            return '- -'
          }
        }
      }, {
        title: '商品种类',
        ellipsis: true,
        dataIndex: 'orderPrice',
        customRender: (text, row, index) => {
          if (text !== null) {
            return row.orderDetails.length + '种'
          } else {
            return '- -'
          }
        }
      }, {
        title: '购买人',
        ellipsis: true,
        dataIndex: 'userName',
        scopedSlots: { customRender: 'userNameShow' }
      }, {
        title: '下单时间',
        ellipsis: true,
        dataIndex: 'createDate',
        customRender: (text, row, index) => {
          if (text !== null) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '操作',
        dataIndex: 'operation',
        scopedSlots: {customRender: 'operation'}
      }]
    }
  },
  mounted () {
    this.fetch()
  },
  methods: {
    completeOrder (record) {
      let that = this
      this.$confirm({
        title: '确认出餐',
        content: `确定要将订单 ${record.code} 标记为已出餐吗？`,
        centered: true,
        onOk () {
          that.$put('/cos/order-info', {
            id: record.id,
            orderStatus: '1'
          }).then(() => {
            that.$message.success('出餐成功')
            that.fetch()
          })
        }
      })
    },
    getImageUrl (imagePath) {
      if (!imagePath) return ''
      if (imagePath.startsWith('http')) {
        return imagePath
      }
      return `http://127.0.0.1:9527/imagesWeb/${imagePath}`
    },
    getTypeName (type) {
      const typeMap = {
        1: '主食',
        2: '菜品',
        3: '小食',
        4: '饮品'
      }
      return typeMap[type] || '其他'
    },
    getTypeColor (type) {
      const colorMap = {
        1: 'green',
        2: 'blue',
        3: 'orange',
        4: 'cyan'
      }
      return colorMap[type] || 'default'
    },
    getTotalQuantity (details) {
      if (!details || !Array.isArray(details)) return 0
      return details.reduce((sum, item) => sum + (item.quantity || 0), 0)
    },
    view (row) {
      this.orderView.data = row
      this.orderView.visiable = true
    },
    handleOrderViewClose () {
      this.orderView.visiable = false
    },
    handleOrderCheckClose () {
      this.orderView.visiable = false
      this.$message.success('审核成功')
      this.fetch()
    },
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleDeptChange (value) {
      this.queryParams.deptId = value || ''
    },
    batchDelete () {
      if (!this.selectedRowKeys.length) {
        this.$message.warning('请选择需要删除的记录')
        return
      }
      let that = this
      this.$confirm({
        title: '确定删除所选中的记录?',
        content: '当您点击确定按钮后，这些记录将会被彻底删除',
        centered: true,
        onOk () {
          let ids = that.selectedRowKeys.join(',')
          that.$delete('/cos/order-info/' + ids).then(() => {
            that.$message.success('删除成功')
            that.selectedRowKeys = []
            that.search()
          })
        },
        onCancel () {
          that.selectedRowKeys = []
        }
      })
    },
    search () {
      let {sortedInfo, filteredInfo} = this
      let sortField, sortOrder
      // 获取当前列的排序和列的过滤规则
      if (sortedInfo) {
        sortField = sortedInfo.field
        sortOrder = sortedInfo.order
      }
      this.fetch({
        sortField: sortField,
        sortOrder: sortOrder,
        ...this.queryParams,
        ...filteredInfo
      })
    },
    reset () {
      // 取消选中
      this.selectedRowKeys = []
      // 重置分页
      this.$refs.TableInfo.pagination.current = this.pagination.defaultCurrent
      if (this.paginationInfo) {
        this.paginationInfo.current = this.pagination.defaultCurrent
        this.paginationInfo.pageSize = this.pagination.defaultPageSize
      }
      // 重置列过滤器规则
      this.filteredInfo = null
      // 重置列排序规则
      this.sortedInfo = null
      // 重置查询参数
      this.queryParams = {}
      this.fetch()
    },
    handleTableChange (pagination, filters, sorter) {
      // 将这三个参数赋值给Vue data，用于后续使用
      this.paginationInfo = pagination
      this.filteredInfo = filters
      this.sortedInfo = sorter

      this.fetch({
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...this.queryParams,
        ...filters
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      if (this.paginationInfo) {
        // 如果分页信息不为空，则设置表格当前第几页，每页条数，并设置查询分页参数
        this.$refs.TableInfo.pagination.current = this.paginationInfo.current
        this.$refs.TableInfo.pagination.pageSize = this.paginationInfo.pageSize
        params.size = this.paginationInfo.pageSize
        params.current = this.paginationInfo.current
      } else {
        // 如果分页信息为空，则设置为默认值
        params.size = this.pagination.defaultPageSize
        params.current = this.pagination.defaultCurrent
      }
      if (params.orderStatus === undefined) {
        delete params.orderStatus
      }
      this.$get('/cos/order-info/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        const pagination = {...this.pagination}
        pagination.total = data.total
        this.dataSource = data.records
        this.pagination = pagination
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  },
  watch: {}
}
</script>
<style lang="less" scoped>
@import "../../../../static/less/Common";

.order-detail-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafbfc 100%);
  border-radius: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  padding-bottom: 12px;
  border-bottom: 2px solid #e8e8e8;
}

.detail-card {
  transition: all 0.3s ease;
  border-radius: 8px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
}

.detail-image-wrapper {
  width: 100%;
  height: 120px;
  margin-bottom: 12px;
  border-radius: 6px;
  overflow: hidden;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.commodity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.detail-info {
  flex: 1;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  gap: 8px;
}

.commodity-name {
  flex: 1;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-tag {
  font-size: 11px;
  flex-shrink: 0;
}

.commodity-desc {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 38px;
}

.price-quantity {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
}

.price-item, .quantity-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.label {
  color: #8c8c8c;
  margin-right: 4px;
}

.value {
  font-weight: 500;

  &.price {
    color: #ff4d4f;
    font-size: 14px;
  }

  &.quantity {
    color: #1890ff;
    font-size: 13px;
  }
}

.subtotal {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed #e8e8e8;
}

.subtotal-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
}

.subtotal-value {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 700;
}

.order-summary {
  margin-top: 16px;
}

.summary-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0;
}

.summary-item {
  text-align: center;

  &.total {
    .summary-value {
      font-size: 20px;
    }
  }
}

.summary-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-right: 8px;
}

.summary-value {
  font-size: 16px;
  color: #262626;
  font-weight: 600;

  &.highlight {
    color: #ff4d4f;
    font-size: 22px;
  }
}

.no-data {
  padding: 40px 20px;
  text-align: center;
}
</style>
