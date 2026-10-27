function userMarkdownSetup(md) {}
function userEleventySetup(eleventyConfig) {
  eleventyConfig.addPassthroughCopy({ "src/site/assets": "assets" });
}
exports.userMarkdownSetup = userMarkdownSetup;
exports.userEleventySetup = userEleventySetup;
