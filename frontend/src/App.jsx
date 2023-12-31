import { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import SharedLayout from './SharedLayout.jsx'
import Home from './NavBar/Home/Home.jsx';
import OurTeam from './NavBar/OurTeam/OurTeam.jsx';
import Jobs from './NavBar/Jobs/Jobs.jsx';
import LogIn from './NavBar/LogIn/LogIn.jsx';
import Account from './NavBar/LogIn/Account.jsx';
import Error from './Error.jsx';

const App = () => {

  const [job, setJob] = useState({});
  const [user, setUser] = useState(null);
  const [appliedJobs, setAppliedJobs] = useState([]);

  return (
    <Routes>
      <Route path='/' element={<SharedLayout job={job} user={user} appliedJobs={appliedJobs} setAppliedJobs={setAppliedJobs} />}>
        <Route index element={<Home />} />
        <Route path='our-team' element={<OurTeam />} />
        <Route path='contact-us' />
        <Route path='jobs' element={<Jobs setJob={setJob} user={user} appliedJobs={appliedJobs} setAppliedJobs={setAppliedJobs} />} />
        <Route path='log-in' element={<LogIn user={user} setUser={setUser} setAppliedJobs={setAppliedJobs} />} />
        <Route path='account' element={<Account user={user} setUser={setUser} setAppliedJobs={setAppliedJobs} />} />
      </Route>
      <Route path='*' element={<Error />} />
    </Routes>
  );
};

export default App;
