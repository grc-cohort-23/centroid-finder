
import { Link } from 'react-router-dom';

export default function VideoList({ videos }) {

  if (videos.length === 0) {
    return <p>No videos available.</p>;
  }

  return (
    <div>
      {videos.map((video) => (
        <div key={video}>
          <Link to={`/preview/${encodeURIComponent(video)}`}>
            {video}
          </Link>
        </div>
      ))}
    </div>
  );
}
