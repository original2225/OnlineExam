export const getUploadHeaders = () => {
  const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
  return user.token ? { token: user.token } : {}
}
