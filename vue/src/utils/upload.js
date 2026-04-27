export const getUploadHeaders = () => {
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  return user.token ? { token: user.token } : {}
}
