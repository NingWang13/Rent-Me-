const app = getApp();

Component({
  data: {
    isElderMode: false,
    fontSize: {
      primary: 14,
      title: 18,
      button: 18,
      caption: 12
    },
    buttonHeight: 44,
    clickArea: 44,
    lineHeight: 1.5,
    contrastRatio: 3
  },

  lifetimes: {
    attached() {
      this.checkElderMode();
    }
  },

  methods: {
    checkElderMode() {
      const elderMode = wx.getStorageSync('elderMode');
      if (elderMode) {
        this.setData({
          isElderMode: true,
          fontSize: {
            primary: 18,
            title: 24,
            button: 18,
            caption: 14
          },
          buttonHeight: 56,
          clickArea: 64,
          lineHeight: 2,
          contrastRatio: 4.5
        });
      }
    },

    switchElderMode() {
      const newMode = !this.data.isElderMode;
      this.setData({
        isElderMode: newMode
      });
      app.setElderMode(newMode);
      if (newMode) {
        this.setData({
          fontSize: {
            primary: 18,
            title: 24,
            button: 18,
            caption: 14
          },
          buttonHeight: 56,
          clickArea: 64,
          lineHeight: 2,
          contrastRatio: 4.5
        });
      } else {
        this.setData({
          fontSize: {
            primary: 14,
            title: 18,
            button: 18,
            caption: 12
          },
          buttonHeight: 44,
          clickArea: 44,
          lineHeight: 1.5,
          contrastRatio: 3
        });
      }
    }
  }
});
