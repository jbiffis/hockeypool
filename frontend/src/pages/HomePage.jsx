import { Link } from 'react-router-dom';

function HomePage() {
  return (
    <div className="home-page">
      <h1>Playoff Pool</h1>
      <p>Hockey playoff pool app</p>
      <Link to="/admin" className="btn btn-primary">Go to Admin</Link>
    </div>
  );
}

export default HomePage;
