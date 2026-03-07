/**
 * 活动状态枚举
 */
export const ACTIVITY_STATUS = {
  PENDING_START: 0,  // 待启动
  RUNNING: 1,        // 运行中
  FINISHED: 2,       // 已结项
  PENDING_AUDIT: 3,  // 待审核
  REJECTED: 4        // 已拒绝
}

/**
 * 状态名称映射
 */
const STATUS_NAMES = {
  0: '待启动',
  1: '运行中',
  2: '已结项',
  3: '待审核',
  4: '已拒绝'
}

/**
 * 状态标签类型映射
 */
const STATUS_TYPES = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'warning',
  4: 'danger'
}

/**
 * 获取状态名称
 */
export function getStatusName(status) {
  return STATUS_NAMES[status] || '未知'
}

/**
 * 获取状态标签类型
 */
export function getStatusType(status) {
  return STATUS_TYPES[status] || 'info'
}

/**
 * 格式化日期 (YYYY-MM-DD)
 */
export function formatDate(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}

/**
 * 格式化日期时间 (YYYY-MM-DD HH:mm)
 */
export function formatDateTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

/**
 * 格式化时间范围
 */
export function formatTimeRange(startTime, endTime) {
  return `${formatDateTime(startTime)} ~ ${formatDateTime(endTime)}`
}

/**
 * 计算报名比例
 */
export function getRegistRatio(current, total) {
  if (!total) return 0
  return current / total
}

/**
 * 获取报名人数样式类
 */
export function getCountClass(current, total) {
  const ratio = getRegistRatio(current, total)
  if (ratio >= 1) return 'text-danger'
  if (ratio >= 0.8) return 'text-warning'
  return 'text-success'
}