const app = getApp();

Page({
  data: {
    userId: null,
    step: 1,
    phone: '',
    verificationCode: '',
    countdown: 0,
    loading: false
  },

  onLoad(options) {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        userId: userInfo.id,
        phone: userInfo.phone
      });
    }
  },

  onInputCode(e) {
    this.setData({
      verificationCode: e.detail.value
    });
  },

  sendVerificationCode() {
    if (this.data.countdown > 0) {
      return;
    }

    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/send-sms`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      data: {
        phone: this.data.phone,
        type: 'verify'
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '验证码已发送',
            icon: 'success'
          });
          this.startCountdown();
        } else {
          wx.showToast({
            title: res.data.message || '发送失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  startCountdown() {
    this.setData({ countdown: 60 });
    const timer = setInterval(() => {
      if (this.data.countdown <= 0) {
        clearInterval(timer);
        return;
      }
      this.setData({
        countdown: this.data.countdown - 1
      });
    }, 1000);
  },

  verifyCode() {
    if (this.data.verificationCode.length !== 6) {
      wx.showToast({
        title: '请输入6位验证码',
        icon: 'none'
      });
      return;
    }

    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/verify-sms`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      data: {
        phone: this.data.phone,
        code: this.data.verificationCode
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '验证成功',
            icon: 'success'
          });
          this.nextStep();
        } else {
          wx.showToast({
            title: res.data.message || '验证失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  nextStep() {
    this.setData({
      step: this.data.step + 1
    });
  },

  verifyIdCard() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '识别中...' });
        wx.request({
          url: `${app.globalData.apiBaseUrl}/user/verify/id-card`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${app.globalData.token}`,
            'Content-Type': 'multipart/form-data'
          },
          formData: {
            userId: this.data.userId
          },
          success: (res) => {
            if (res.data.code === 200) {
              wx.showToast({
                title: '身份证认证成功',
                icon: 'success'
              });
              this.nextStep();
            } else {
              wx.showToast({
                title: res.data.message || '认证失败',
                icon: 'none'
              });
            }
          },
          fail: () => {
            wx.showToast({
              title: '网络错误',
              icon: 'none'
            });
          },
          complete: () => {
            wx.hideLoading();
          }
        });
      }
    });
  },

  verifyFace() {
    wx.checkIsSupportFacialRecognition({
      success: () => {
        wx.startFacialRecognition({
          success: (res) => {
            wx.request({
              url: `${app.globalData.apiBaseUrl}/user/verify/face`,
              method: 'POST',
              header: {
                'Authorization': `Bearer ${app.globalData.token}`
              },
              data: {
                userId: this.data.userId,
                result: res.result
              },
              success: (res) => {
                if (res.data.code === 200) {
                  wx.showToast({
                    title: '人脸认证成功',
                    icon: 'success'
                  });
                  this.finishAuth();
                } else {
                  wx.showToast({
                    title: res.data.message || '认证失败',
                    icon: 'none'
                  });
                }
              }
            });
          },
          fail: () => {
            wx.showToast({
              title: '人脸识别失败',
              icon: 'none'
            });
          }
        });
      },
      fail: () => {
        wx.showToast({
          title: '设备不支持人脸识别',
          icon: 'none'
        });
      }
    });
  },

  finishAuth() {
    wx.navigateBack();
  }
});
