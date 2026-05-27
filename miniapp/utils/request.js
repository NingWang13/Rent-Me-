const app = getApp();

const DEFAULT_OPTIONS = {
  timeout: 10000,
  retryCount: 2,
  showLoading: false,
  loadingText: '加载中...',
  showError: true
};

const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '请先登录',
  403: '没有权限访问',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂时不可用',
  504: '网关超时'
};

class RequestInterceptor {
  constructor() {
    this.requestInterceptors = [];
    this.responseInterceptors = [];
    this.errorInterceptors = [];
  }

  addRequestInterceptor(interceptor) {
    this.requestInterceptors.push(interceptor);
  }

  addResponseInterceptor(interceptor) {
    this.responseInterceptors.push(interceptor);
  }

  addErrorInterceptor(interceptor) {
    this.errorInterceptors.push(interceptor);
  }

  async executeRequestInterceptors(config) {
    let finalConfig = { ...config };
    for (const interceptor of this.requestInterceptors) {
      finalConfig = await interceptor(finalConfig);
    }
    return finalConfig;
  }

  async executeResponseInterceptors(response) {
    let finalResponse = response;
    for (const interceptor of this.responseInterceptors) {
      finalResponse = await interceptor(finalResponse);
    }
    return finalResponse;
  }

  async executeErrorInterceptors(error) {
    for (const interceptor of this.errorInterceptors) {
      await interceptor(error);
    }
  }
}

const interceptorManager = new RequestInterceptor();

interceptorManager.addRequestInterceptor(async (config) => {
  const token = wx.getStorageSync('token');
  if (token) {
    config.header = config.header || {};
    config.header['Authorization'] = `Bearer ${token}`;
  }
  config.header['Content-Type'] = config.header['Content-Type'] || 'application/json';
  config.header['X-Request-Id'] = generateRequestId();
  return config;
});

interceptorManager.addResponseInterceptor(async (response) => {
  const { statusCode, data } = response;

  if (statusCode === 401) {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.redirectTo({ url: '/pages/auth/login/login' });
    throw new Error('登录已过期，请重新登录');
  }

  if (data && data.code !== undefined && data.code !== 200) {
    throw new Error(data.message || ERROR_MESSAGES[statusCode] || '请求失败');
  }

  return data;
});

interceptorManager.addErrorInterceptor(async (error) => {
  console.error('Request error:', error);
  wx.showToast({
    title: error.message || '网络请求失败',
    icon: 'none',
    duration: 2000
  });
});

function generateRequestId() {
  return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

async function request(url, options = {}) {
  const mergedOptions = { ...DEFAULT_OPTIONS, ...options };
  let config = {
    url: url.startsWith('http') ? url : `${app.globalData.apiBaseUrl}${url}`,
    method: mergedOptions.method || 'GET',
    data: mergedOptions.data,
    header: mergedOptions.header || {},
    timeout: mergedOptions.timeout
  };

  config = await interceptorManager.executeRequestInterceptors(config);

  if (mergedOptions.showLoading) {
    wx.showLoading({ title: mergedOptions.loadingText, mask: true });
  }

  let lastError;
  for (let attempt = 0; attempt <= mergedOptions.retryCount; attempt++) {
    try {
      const response = await wxRequest(config);
      const result = await interceptorManager.executeResponseInterceptors(response);

      if (mergedOptions.showLoading) {
        wx.hideLoading();
      }

      return result;
    } catch (error) {
      lastError = error;
      if (attempt < mergedOptions.retryCount) {
        await delay(1000 * (attempt + 1));
      }
    }
  }

  if (mergedOptions.showLoading) {
    wx.hideLoading();
  }

  if (mergedOptions.showError) {
    await interceptorManager.executeErrorInterceptors(lastError);
  }

  throw lastError;
}

function wxRequest(config) {
  return new Promise((resolve, reject) => {
    wx.request({
      ...config,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res);
        } else {
          reject(new Error(ERROR_MESSAGES[res.statusCode] || `HTTP ${res.statusCode}`));
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络请求失败'));
      }
    });
  });
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

const get = (url, data, options) => request(url, { method: 'GET', data, ...options });
const post = (url, data, options) => request(url, { method: 'POST', data, ...options });
const put = (url, data, options) => request(url, { method: 'PUT', data, ...options });
const del = (url, data, options) => request(url, { method: 'DELETE', data, ...options });

module.exports = {
  request,
  get,
  post,
  put,
  del,
  interceptorManager
};
