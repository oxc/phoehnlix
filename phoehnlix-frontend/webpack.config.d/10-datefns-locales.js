const webpack = require('webpack');

var supportedLocales = ["de"];

config.plugins = config.plugins || [];
config.plugins.push(
  new webpack.ContextReplacementPlugin(
    /date\-fns[\/\\]/,
    new RegExp(`[/\\\\\](${supportedLocales.join('|')})[/\\\\\]`)
  )
);

