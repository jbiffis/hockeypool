const VIDEO_RE = /\.(mp4|webm|mov)(\?|#|$)/i;

export const isVideoUrl = (url) => typeof url === 'string' && VIDEO_RE.test(url);

export const markdownComponents = {
  a: ({ href, children, ...props }) => {
    if (isVideoUrl(href)) {
      return (
        <video
          src={href}
          controls
          preload="metadata"
          style={{ maxWidth: '100%', borderRadius: 6, display: 'block', margin: '8px 0' }}
        />
      );
    }
    return <a href={href} target="_blank" rel="noopener noreferrer" {...props}>{children}</a>;
  },
};
