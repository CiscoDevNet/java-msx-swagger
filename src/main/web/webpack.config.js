const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = env => {
  return {
    entry: {
      "msx-swagger-ui": ['./js/msx-swagger-ui.js'],
    },
    output: {
      path: path.resolve(env.OUTPUT_DIR),
      filename: '[name].js',
    },
    resolveLoader: {
      modules: [path.join(__dirname, "node_modules")],
    },
    resolve: {
      extensions: ['.js', '.jsx'],
      modules: [
        path.join(__dirname, "./js"),
        "node_modules"
      ]
    },
    plugins: [
      new BundleAnalyzerPlugin({
        analyzerMode: "static",
        openAnalyzer: false
      })
    ],
    module: {
      rules: [
        {
          test: /\.(js(x)?)(\?.*)?$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader'
          }
        },
        {
          test: /\.svg$/,
          exclude: /node_modules/,
          use: {
            loader: 'react-svg-loader'
          }
        }
      ]
    }
  }
};