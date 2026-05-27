const app = getApp();

Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    }
  },

  data: {
    currentStep: 0,
    steps: [
      {
        image: '/images/guide/welcome.png',
        title: '欢迎使用三代互助社区',
        description: '在这里，您可以发起互助请求，也可以帮助他人获得积分'
      },
      {
        image: '/images/guide/publish.png',
        title: '发布互助需求',
        description: '点击"发布"按钮，描述您的需求，设置积分奖励'
      },
      {
        image: '/images/guide/accept.png',
        title: '接受他人的请求',
        description: '浏览他人的求助信息，用您的积分帮助他们'
      },
      {
        image: '/images/guide/credit.png',
        title: '积分系统',
        description: '帮助他人获得积分，使用积分获得帮助'
      }
    ]
  },

  methods: {
    nextStep() {
      if (this.data.currentStep < this.data.steps.length - 1) {
        this.setData({
          currentStep: this.data.currentStep + 1
        });
      } else {
        this.finishGuide();
      }
    },

    prevStep() {
      if (this.data.currentStep > 0) {
        this.setData({
          currentStep: this.data.currentStep - 1
        });
      }
    },

    skipGuide() {
      this.finishGuide();
    },

    finishGuide() {
      wx.setStorageSync('hasSeenGuide', true);
      this.triggerEvent('close');
    },

    onTapDots(e) {
      const index = e.currentTarget.dataset.index;
      this.setData({
        currentStep: index
      });
    }
  }
});
