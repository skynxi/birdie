/* Example data for the demo preview only. The real app starts empty. */
window.BIRDIE_KEY="birdie.demo.v1";
window.BIRDIE_SEED=function(){
 let seed=11;const r=()=>(seed=seed*16807%2147483647)/2147483647;
 const T=new Date();T.setHours(0,0,0,0);
 const iso=d=>d.getFullYear()+"-"+String(d.getMonth()+1).padStart(2,"0")+"-"+String(d.getDate()).padStart(2,"0");
 const add=(d,n)=>{const x=new Date(d);x.setDate(x.getDate()+n);return x},day=n=>iso(add(T,n)),md=n=>{const x=add(T,n);return{m:x.getMonth(),d:x.getDate()}};
 let id=1;const nid=()=>id++;
 const people=[
  {id:nid(),n:"Riya",bd:{...md(3),y:1998},rem:[1,7],gift:true,bought:false,idea:"Noise-cancelling headphones",cad:0,co:"",tag:"",stage:"",note:"Loves indie music and coffee",last:day(0),next:day(14)},
  {id:nid(),n:"Mom",bd:{...md(18),y:1970},rem:[1,7],gift:true,bought:true,idea:"Silk scarf",cad:0,co:"",tag:"",stage:"",note:"",last:day(0),next:day(14)},
  {id:nid(),n:"Neha",bd:{...md(0),y:1999},rem:[0,1],gift:true,bought:true,idea:"Book",cad:0,co:"",tag:"",stage:"",note:"",last:day(0),next:day(14)},
  {id:nid(),n:"Arjun",bd:{...md(54),y:1996},rem:[1],gift:false,bought:false,idea:"",cad:0,co:"",tag:"",stage:"",note:"",last:day(0),next:day(14)},
  {id:nid(),n:"Aarav Mehta",bd:null,rem:[1],gift:false,bought:false,idea:"",cad:14,co:"Mehta Textiles",tag:"Client",stage:"Active",note:"Wants festive packaging by Jan",last:day(-17),next:day(-3)},
  {id:nid(),n:"Sana Iqbal",bd:{...md(41),y:1994},rem:[1],gift:false,bought:false,idea:"",cad:7,co:"Brightline Media",tag:"Lead",stage:"Proposal sent",note:"Sent branding proposal, ₹60k",last:day(-9),next:day(-2)},
  {id:nid(),n:"Kavya Rao",bd:null,rem:[1],gift:false,bought:false,idea:"",cad:7,co:"Nimbus Labs",tag:"Lead",stage:"Talking",note:"",last:day(-4),next:day(0)},
  {id:nid(),n:"Rohan Gupta",bd:null,rem:[1],gift:false,bought:false,idea:"",cad:30,co:"Gupta & Co",tag:"Partner",stage:"Active",note:"",last:day(-24),next:day(6)}];
 const dates=[{id:nid(),n:"GST filing",...md(9),rem:[1,7]},{id:nid(),n:"Parents’ anniversary",...md(30),rem:[1,7]},{id:nid(),n:"Car insurance renewal",...md(47),rem:[7]}];
 const calls=[{id:nid(),pid:7,d:day(0),t:"15:00",lead:30,topic:"Pricing walkthrough",rep:"none",done:false},{id:nid(),pid:6,d:day(1),t:"11:30",lead:60,topic:"Proposal feedback",rep:"none",done:false},{id:nid(),pid:5,d:day(3),t:"10:00",lead:30,topic:"Renewal terms",rep:"monthly",done:false}];
 const todos=[
  {id:nid(),t:"Send invoice to Mehta Textiles",d:day(0),rem:"",rep:"none",star:true,subs:[],done:true,at:"10:12",dd:day(0)},
  {id:nid(),t:"Pick up Riya’s headphones",d:day(0),rem:"18:30",rep:"none",star:false,subs:[],done:false},
  {id:nid(),t:"Finish Brightline moodboard",d:day(0),rem:"",rep:"none",star:true,subs:[{t:"Pick 3 type pairings",done:true},{t:"Colour options",done:true},{t:"Export PDF",done:false}],done:false},
  {id:nid(),t:"Water the plants",d:day(0),rem:"",rep:"daily",star:false,subs:[],done:true,at:"08:05",dd:day(0)},
  {id:nid(),t:"Reply to Orchid Studio brief",d:day(-1),rem:"",rep:"none",star:false,subs:[],done:false},
  {id:nid(),t:"Post weekly reel",d:day(0),rem:"19:00",rep:"weekly",star:false,subs:[],done:false,gid:900},
  {id:nid(),t:"Book GST appointment with CA",d:day(1),rem:"11:00",rep:"none",star:false,subs:[],done:false},
  {id:nid(),t:"Pay rent",d:day(2),rem:"10:00",rep:"monthly",star:true,subs:[],done:false},
  {id:nid(),t:"Plan Riya’s birthday dinner",d:day(2),rem:"",rep:"none",star:false,subs:[],done:false}];
 const past=["Client call notes","Update portfolio","Gym","Grocery run","Logo revisions","Reply to emails","Read 20 pages","Backup files","Call Mom","Sketch packaging","Invoice follow-up","Clean desk"];
 for(let i=1;i<45;i++){const n=Math.floor(r()*5);for(let j=0;j<n;j++)todos.push({id:nid(),t:past[Math.floor(r()*past.length)],d:day(-i),rem:"",rep:"none",star:false,subs:[],done:true,at:String(9+Math.floor(r()*11)).padStart(2,"0")+":"+String(Math.floor(r()*60)).padStart(2,"0"),dd:day(-i)})}
 for(let w=1;w<4;w++){const dd=day(-7*w);if(dd.slice(0,7)===day(0).slice(0,7))todos.push({id:nid(),t:"Post weekly reel",d:dd,rem:"19:00",rep:"weekly",star:false,subs:[],done:true,at:"19:20",dd,gid:900,nx:1})}
 const mk=(n,icon,color,days,rate,len,rem)=>{const log={};for(let i=1;i<len;i++){const d=add(T,-i);if(days.includes(d.getDay())&&r()<rate(i))log[iso(d)]=1}return{id:nid(),n,icon,color,days,rem:rem||"",log,created:day(-len)}};
 const all=[0,1,2,3,4,5,6];
 const habits=[mk("Read 20 pages","book","var(--h4)",all,i=>i<13?1:.7,120,"21:30"),mk("Gym","dumbbell","var(--h3)",[1,3,5,6],()=>.8,120,"07:00"),mk("Meditate","lotus","var(--h2)",all,i=>i<6?1:.55,90),mk("Journal","journal","var(--h1)",all,()=>.6,60,"22:15"),mk("No sugar","apple","var(--h4)",[1,2,3,4,5],()=>.75,45),mk("Practice guitar","music","var(--h2)",[2,4,6],()=>.7,80)];
 habits[0].log[day(0)]=1;habits[2].log[day(0)]=1;
 const walk={id:nid(),n:"Walk",icon:"steps",color:"var(--h2)",days:all,rem:"",kind:"count",target:8000,unit:"steps",step:1000,log:{},created:day(-60)};for(let i=1;i<60;i++){if(r()<.85)walk.log[day(-i)]=Math.round((3000+r()*8000)/500)*500}walk.log[day(0)]=5000;for(let i=1;i<9;i++)walk.log[day(-i)]=8000+Math.round(r()*3000);habits.push(walk);
 const water={goal:8,log:{}};for(let i=0;i<30;i++)water.log[day(-i)]=i===0?5:4+Math.floor(r()*5);
 const sleep=[];for(let i=0;i<21;i++){const b=["23:10","00:20","22:50","23:40","01:05","23:00"][Math.floor(r()*6)],w=["06:50","07:30","07:10","08:00","06:40"][Math.floor(r()*5)];sleep.push({d:day(-i),b,w})}
 const w=[];for(let i=75;i>=0;i--){if(i>4&&r()<.15)continue;const base=75.2-(75-i)*.038,n=1+(r()<.3);for(let j=0;j<n;j++)w.push({d:day(-i),kg:Math.round((base+(r()-.5)*.9)*10)/10})}
const JL={1:["Rough day. Early night.","Client call went badly, need to regroup.","Headache all day, skipped everything."],2:["Tired and behind on emails.","Meh day, too much scrolling.","Rained all day, stayed in."],3:["Okay day. Finished the pitch deck draft.","Quiet day, cooked dal and watched a film.","Busy but fine. Groceries done."],4:["Gym felt great today.","Long chat with Mom, felt nice.","Sent the logo options, client loved two of them.","Read 40 pages on the metro.","Coffee with Riya after ages!"],5:["Best day in weeks. Landed the new client!","Sunset walk at Lodhi Garden with friends.","Hit a new PR at the gym and the deck is DONE.","Surprise dinner for Arjun went perfectly.","Paid off the last of the laptop EMI!"]};
 const APPS=[["com.instagram.android","Instagram",70],["com.whatsapp","WhatsApp",45],["com.google.android.youtube","YouTube",35],["com.android.chrome","Chrome",22],["com.spotify.music","Spotify",15],["com.google.android.gm","Gmail",9],["com.linkedin.android","LinkedIn",8]];
 const usage={};for(let i=0;i<10;i++){const k=day(-i),f=i===0?(new Date().getHours()/22):1,apps=APPS.map(([pkg,name,b])=>({pkg,name,min:Math.max(1,Math.round(b*(0.6+r()*0.8)*f))})).sort((a,b)=>b.min-a.min);usage[k]={total:apps.reduce((x,a)=>x+a.min,0)+Math.round(20*f),unlocks:Math.round((60+r()*40)*f),first:new Date(T.getFullYear(),T.getMonth(),T.getDate()-i,9,10+Math.round(r()*40)).getTime(),last:0,apps}}
 habits.push({id:950,n:"Instagram under 1h",icon:"phoneoff",color:"var(--h2)",days:[0,1,2,3,4,5,6],rem:"",kind:"screen",target:60,app:"com.instagram.android",appName:"Instagram",unit:"",step:1,log:{},created:day(-20)},{id:951,n:"Screen time under 4h",icon:"phone",color:"var(--h3)",days:[0,1,2,3,4,5,6],rem:"",kind:"screen",target:240,app:"",unit:"",step:1,log:{},created:day(-20)});
 const sb=new Date(T.getFullYear(),T.getMonth(),T.getDate()-1,23,48).getTime(),sw=new Date(T.getFullYear(),T.getMonth(),T.getDate(),7,52).getTime();
 const moods=[];const doy=Math.floor((T-new Date(T.getFullYear(),0,1))/864e5);for(let i=1;i<=doy;i++){if(r()<.2&&i>20)continue;const k=day(-i),gym=habits[1].log[k],rd=habits[0].log[k];let v=2+Math.floor(r()*3)+(gym&&r()<.7?1:0)+(rd&&r()<.4?1:0);const vv=Math.max(1,Math.min(5,v)),jl=JL[vv];moods.push({d:k,v:vv,note:r()<.65?jl[Math.floor(r()*jl.length)]:""})}
 const mon=x=>{const d=new Date(T);d.setHours(0,0,0,0);d.setDate(d.getDate()-((d.getDay()+6)%7));return iso(add(d,x*7))};
 const mstart=(k)=>iso(new Date(T.getFullYear(),T.getMonth()+k,1)),mend=(k)=>iso(new Date(T.getFullYear(),T.getMonth()+k+1,0));
 const goals=[
  {id:nid(),p:"week",s:"Work out 4 times",target:4,unit:"workouts",a:"Book the Monday class",r:"More energy for client work",due:iso(add(new Date(mon(0)+"T00:00"),6)),start:mon(0),cur:3,track:"habit",hid:habits[1].id,per:1},
  {id:nid(),p:"week",s:"Read 100 pages",target:100,unit:"pages",a:"20 pages before bed",r:"Wind down without my phone",due:iso(add(new Date(mon(0)+"T00:00"),6)),start:mon(0),cur:60,track:"habit",hid:habits[0].id,per:20},
  {id:nid(),p:"week",s:"Send 5 pitch emails",target:5,unit:"emails",a:"Shortlist 10 brands on Monday",r:"Fill January’s project calendar",due:iso(add(new Date(mon(-1)+"T00:00"),6)),start:mon(-1),cur:5},
  {id:nid(),p:"month",s:"Save ₹20,000",target:20000,unit:"rupees",a:"Move ₹5,000 every Sunday",r:"Emergency fund for the agency",due:mend(0),start:mstart(0),cur:12500},
  {id:900,p:"month",s:"Post 4 reels",target:4,unit:"reels",a:"Batch-film on Sundays",r:"Grow the agency’s Instagram",due:mend(0),start:mstart(0),cur:0,track:"task",per:1},
  {id:nid(),p:"month",s:"Land 2 new clients",target:2,unit:"clients",a:"Follow up on every proposal within 3 days",r:"Steady income from Jan",due:mend(0),start:mstart(0),cur:1},
  {id:nid(),p:"year",s:"Read 24 books",target:24,unit:"books",a:"Always have the next one ready",r:"Read more than I scroll",due:iso(new Date(T.getFullYear(),11,31)),start:iso(new Date(T.getFullYear(),0,1)),cur:19},
  {id:nid(),p:"year",s:"Grow the agency to ₹12 lakh",target:1200000,unit:"rupees",a:"Raise rates for new clients in Q2",r:"Hire a junior designer next year",due:iso(new Date(T.getFullYear(),11,31)),start:iso(new Date(T.getFullYear(),0,1)),cur:940000}];
 const tx=[],dt=(k,dd)=>{const x=new Date(T.getFullYear(),T.getMonth()+k,1),last=new Date(x.getFullYear(),x.getMonth()+1,0).getDate();x.setDate(Math.min(dd,last));return iso(x)};
 const rentId=nid(),netId=nid(),gymId=nid(),figId=nid();
 tx.push({id:rentId,k:"out",amt:18000,cat:"Rent",note:"Flat rent",d:dt(-5,3),rep:"monthly"},{id:netId,k:"out",amt:649,cat:"Fun",note:"Netflix",d:dt(-5,12),rep:"monthly"},{id:gymId,k:"out",amt:1500,cat:"Health",note:"Gym membership",d:dt(-5,5),rep:"monthly"},{id:figId,k:"out",amt:1250,cat:"Work",note:"Figma",d:dt(-5,8),rep:"monthly"});
 const clients=[["Mehta Textiles",45000],["Brightline Media",28000],["Nimbus Labs",22000],["Gupta & Co",18000],["Orchid Studio",15000]];
 for(let k=-5;k<=0;k++){const maxDay=k===0?T.getDate():28;
  const add1=(o)=>{if(+o.d.slice(8)<=maxDay||k<0)tx.push({id:nid(),rep:"none",...o})};
  for(let c=0;c<2+(r()<.5);c++){const[cn,a]=clients[Math.floor(r()*clients.length)];add1({k:"in",amt:Math.round(a*(.8+r()*.4)/500)*500,cat:"Client payment",note:cn,d:dt(k,3+Math.floor(r()*24))})}
  if(r()<.6)add1({k:"in",amt:Math.round((6+r()*10))*1000,cat:"Freelance",note:["Logo design","Social media pack","Packaging mockups"][Math.floor(r()*3)],d:dt(k,5+Math.floor(r()*20))});
  for(let i=0;i<4;i++)add1({k:"out",amt:Math.round((1200+r()*1800)/10)*10,cat:"Groceries",note:["BigBasket","Blinkit","Local market"][Math.floor(r()*3)],d:dt(k,2+i*7+Math.floor(r()*4))});
  for(let i=0;i<7;i++)add1({k:"out",amt:Math.round((250+r()*700)/10)*10,cat:"Food",note:["Swiggy","Zomato","Café with Riya","Lunch meeting"][Math.floor(r()*4)],d:dt(k,1+Math.floor(r()*27))});
  for(let i=0;i<6;i++)add1({k:"out",amt:Math.round((120+r()*380)/10)*10,cat:"Transport",note:["Uber","Metro card","Ola","Rapido"][Math.floor(r()*4)],d:dt(k,1+Math.floor(r()*27))});
  add1({k:"out",amt:Math.round((1400+r()*1200)/10)*10,cat:"Bills",note:"Electricity",d:dt(k,10)});add1({k:"out",amt:799,cat:"Bills",note:"Phone + internet",d:dt(k,14)});
  if(r()<.7)add1({k:"out",amt:Math.round((900+r()*3500)/10)*10,cat:"Shopping",note:["Myntra","Amazon","Nykaa"][Math.floor(r()*3)],d:dt(k,4+Math.floor(r()*22))});
  if(r()<.4)add1({k:"out",amt:Math.round((1500+r()*3000)/10)*10,cat:"Gifts",note:"Birthday gift",d:dt(k,4+Math.floor(r()*22))});
  if(k===-2)add1({k:"out",amt:14800,cat:"Travel",note:"Jaipur trip",d:dt(k,18)});
  if(r()<.5)add1({k:"out",amt:Math.round((400+r()*900)/10)*10,cat:"Fun",note:["Movie","Concert tickets","Bowling"][Math.floor(r()*3)],d:dt(k,6+Math.floor(r()*20))})}
 [[rentId,3],[netId,12],[gymId,5],[figId,8]].forEach(([src,dd])=>{const tp=tx.find(t=>t.id===src);for(let k=-4;k<=0;k++){const d=dt(k,dd);if(d<=iso(T))tx.push({...tp,id:nid(),d,src,rep:"none"})}});
 const lt=new Date(Date.now()+90*60000),later=lt.getDate()===T.getDate()?String(lt.getHours()).padStart(2,"0")+":"+String(lt.getMinutes()).padStart(2,"0"):"23:30";
 const reminders=[{id:nid(),t:"Take vitamins",note:"After breakfast",time:"09:30",rep:"daily",d:day(-20),days:[],on:true},{id:nid(),t:"Check the markets",note:"",time:"15:45",rep:"weekdays",d:day(-20),days:[],on:true},{id:nid(),t:"Call home",note:"",time:"19:30",rep:"weekly",d:day(-20),days:[0,3],on:true},{id:nid(),t:"Pay credit card bill",note:"Before the 7th",time:"10:00",rep:"monthly",d:dt(0,5),days:[],on:true},{id:nid(),t:"Charge headphones for tomorrow",note:"",time:later,rep:"once",d:day(0),days:[],on:true},{id:nid(),t:"Stretch break",note:"",time:"16:30",rep:"weekdays",d:day(-5),days:[],on:false}];
 const debts=[{id:nid(),dir:"owe",who:"Riya",amt:2400,paid:0,note:"Concert tickets",due:day(2),done:false,created:day(-6)},{id:nid(),dir:"owe",who:"Mom",amt:5000,paid:2000,note:"Laptop repair",due:"",done:false,created:day(-20)},{id:nid(),dir:"owed",who:"Arjun",amt:1500,paid:0,note:"Dinner split",due:day(0),done:false,created:day(-3)},{id:nid(),dir:"owed",who:"Neha",amt:6000,paid:2000,note:"Camera lens",due:day(10),done:false,created:day(-14)}];
 const pots=[{id:nid(),n:"Goa trip",target:30000,saved:18500,ic:"plane",by:day(60),log:[],done:false},{id:nid(),n:"New iPad",target:45000,saved:12000,ic:"laptop",by:"",log:[],done:false},{id:nid(),n:"Emergency fund",target:100000,saved:100000,ic:"coins",by:"",log:[],done:true}];
 people[0].kit=14;people[0].talked=day(-20);people[1].kit=7;people[1].talked=day(-3);people[3].kit=30;people[3].talked=day(-41);
 return{pots,frozen:{[day(-6)]:1},freezes:2,freezesUsed:1,checkins:6,appear:"dark",outfit:{neck:"scarf",face:"shades"},verSeen:"5.3",usage,notes:[{id:960,t:"Brightline kickoff notes",b:"Client wants a warmer palette, less corporate.\nDeliver 3 logo routes by Friday.\nBudget approved for packaging too!\nNext call: Tuesday 4 PM",kind:"text",items:[],pin:true,c:Date.now()-3*864e5,up:Date.now()-2*36e5},{id:961,t:"Goa packing",b:"",kind:"list",items:[{t:"Sunscreen",done:true},{t:"Swimsuit",done:true},{t:"Power bank",done:false},{t:"Sunglasses",done:false},{t:"Book for the flight",done:false}],pin:false,c:Date.now()-5*864e5,up:Date.now()-864e5},{id:962,t:"Reel ideas",b:"Day in the life of a designer\nBefore/after logo redesign\n3 fonts I always use\nClient feedback memes",kind:"text",items:[],pin:false,c:Date.now()-9*864e5,up:Date.now()-4*864e5}],stOn:true,stSleep:{bed:sb,wake:sw,d:day(0)},debts,reminders,people,dates,calls,todos,habits,water,sleep,goals,w,moods,tx,budgets:{Food:6000,Groceries:8000,Transport:3500,Shopping:4000,Fun:2500,Bills:3500},
  height:165,goal:68,name:"Pragya",skin:"birdie",text:1,inApp:true,sys:true,wRem:true,remT:"08:00",skipLogged:true,bundle:true,escalate:true,quiet:true,review:true,reviewT:"22:00",morning:true,morningT:"09:00",goalNudge:true,sleepGoal:8,moneySum:true,nextId:id+10}};
