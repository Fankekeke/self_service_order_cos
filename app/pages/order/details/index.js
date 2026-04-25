
const app = getApp();
let http = require('../../../utils/request')

Page({
    data: {
        StatusBar: app.globalData.StatusBar,
        CustomBar: app.globalData.CustomBar,
        orderId: null,
        orderInfo: {},
        orderDetails: [],
        totalNutrition: null,
        nutritionAnalysis: null,
        showNutrition: false,
        currentGoods: null
    },

    onLoad: function (options) {
        if (options.orderId) {
            this.setData({
                orderId: options.orderId
            })
            this.getOrderDetail(options.orderId)
        }
    },

    getOrderDetail(orderId) {
        wx.showLoading({
            title: '加载中...',
            mask: true
        })

        http.get('queryOrderDetail', { orderId: orderId }).then((r) => {
            wx.hideLoading()

            if (r && r.code === 0) {
                this.setData({
                    orderInfo: r.orderInfo,
                    orderDetails: r.orderDetails,
                    totalNutrition: r.totalNutrition,
                    nutritionAnalysis: r.nutritionAnalysis
                })

                setTimeout(() => {
                    this.drawNutritionChart()
                }, 500)
            }
        }).catch(() => {
            wx.hideLoading()
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            })
        })
    },

    drawNutritionChart() {
        const query = wx.createSelectorQuery().in(this)
        query.select('#nutritionChart').boundingClientRect()
        query.exec((res) => {
            if (!res[0]) return

            const canvasWidth = res[0].width
            const canvasHeight = res[0].height
            const ctx = wx.createCanvasContext('nutritionChart', this)

            const nutrition = this.data.totalNutrition
            if (!nutrition) return

            const data = [
                { label: '蛋白质', value: nutrition.totalProtein, color: '#2196F3' },
                { label: '脂肪', value: nutrition.totalFat, color: '#4CAF50' },
                { label: '碳水', value: nutrition.totalCarbohydrate, color: '#9C27B0' }
            ]

            const total = data.reduce((sum, item) => sum + item.value, 0)
            const centerX = canvasWidth / 2
            const centerY = canvasHeight / 2
            const radius = Math.min(centerX, centerY) - 20

            let startAngle = -Math.PI / 2

            data.forEach((item, index) => {
                const angle = (item.value / total) * 2 * Math.PI
                const endAngle = startAngle + angle

                ctx.beginPath()
                ctx.moveTo(centerX, centerY)
                ctx.arc(centerX, centerY, radius, startAngle, endAngle)
                ctx.closePath()
                ctx.setFillStyle(item.color)
                ctx.fill()

                const midAngle = startAngle + angle / 2
                const textRadius = radius * 0.7
                const textX = centerX + Math.cos(midAngle) * textRadius
                const textY = centerY + Math.sin(midAngle) * textRadius

                const percent = ((item.value / total) * 100).toFixed(1)
                if (percent > 10) {
                    ctx.setFontSize(12)
                    ctx.setFillStyle('#ffffff')
                    ctx.setTextAlign('center')
                    ctx.setTextBaseline('middle')
                    ctx.fillText(percent + '%', textX, textY)
                }

                startAngle = endAngle
            })

            ctx.beginPath()
            ctx.arc(centerX, centerY, radius * 0.5, 0, 2 * Math.PI)
            ctx.setFillStyle('#ffffff')
            ctx.fill()

            ctx.setFontSize(16)
            ctx.setFillStyle('#333')
            ctx.setTextAlign('center')
            ctx.setTextBaseline('middle')
            ctx.fillText('总计', centerX, centerY - 12)
            ctx.setFontSize(14)
            ctx.setFillStyle('#666')
            ctx.fillText(`${total.toFixed(1)}g`, centerX, centerY + 12)

            ctx.draw()
        })
    },

    drawGoodsNutritionChart() {
        const query = wx.createSelectorQuery().in(this)
        query.select('#goodsNutritionChart').boundingClientRect()
        query.exec((res) => {
            if (!res[0]) return

            const canvasWidth = res[0].width
            const canvasHeight = res[0].height
            const ctx = wx.createCanvasContext('goodsNutritionChart', this)

            const nutrition = this.data.currentGoods.nutritionInfo
            if (!nutrition) return

            const data = [
                { label: '蛋白质', value: nutrition.protein, color: '#2196F3' },
                { label: '脂肪', value: nutrition.fat, color: '#4CAF50' },
                { label: '碳水', value: nutrition.carbohydrate, color: '#9C27B0' },
                { label: '纤维', value: nutrition.fiber, color: '#795548' },
                { label: '糖', value: nutrition.sugar, color: '#E91E63' }
            ]

            const total = data.reduce((sum, item) => sum + item.value, 0)
            const centerX = canvasWidth / 2
            const centerY = canvasHeight / 2
            const radius = Math.min(centerX, centerY) - 20

            let startAngle = -Math.PI / 2

            data.forEach((item) => {
                const angle = (item.value / total) * 2 * Math.PI
                const endAngle = startAngle + angle

                ctx.beginPath()
                ctx.moveTo(centerX, centerY)
                ctx.arc(centerX, centerY, radius, startAngle, endAngle)
                ctx.closePath()
                ctx.setFillStyle(item.color)
                ctx.fill()

                const midAngle = startAngle + angle / 2
                const textRadius = radius * 0.7
                const textX = centerX + Math.cos(midAngle) * textRadius
                const textY = centerY + Math.sin(midAngle) * textRadius

                const percent = ((item.value / total) * 100).toFixed(1)
                if (percent > 8) {
                    ctx.setFontSize(11)
                    ctx.setFillStyle('#ffffff')
                    ctx.setTextAlign('center')
                    ctx.setTextBaseline('middle')
                    ctx.fillText(percent + '%', textX, textY)
                }

                startAngle = endAngle
            })

            ctx.beginPath()
            ctx.arc(centerX, centerY, radius * 0.5, 0, 2 * Math.PI)
            ctx.setFillStyle('#ffffff')
            ctx.fill()

            ctx.setFontSize(14)
            ctx.setFillStyle('#333')
            ctx.setTextAlign('center')
            ctx.setTextBaseline('middle')
            ctx.fillText('营养', centerX, centerY - 10)
            ctx.setFontSize(12)
            ctx.setFillStyle('#666')
            ctx.fillText(`${total.toFixed(1)}g`, centerX, centerY + 10)

            ctx.draw()
        })
    },

    showNutritionDetail(e) {
        const index = e.currentTarget.dataset.index
        const goods = this.data.orderDetails[index]

        this.setData({
            showNutrition: true,
            currentGoods: goods
        })

        setTimeout(() => {
            this.drawGoodsNutritionChart()
        }, 300)
    },

    closeNutritionDetail() {
        this.setData({
            showNutrition: false
        })
    }
})
