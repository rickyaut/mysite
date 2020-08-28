//refer configuration details at https://cli.vuejs.org/config/#global-cli-config
module.exports = {
  outputDir: 'dist/clientlib-site',
  devServer: {
    proxy:{
      '^/content': {
        target: 'http://localhost:4502',
        ws: false,
        changeOrigin: false
      },
      '^/google': {
        target: 'http://www.google.com.au'
      }
    }
  }
}
