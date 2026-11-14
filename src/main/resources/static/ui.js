/* MFU Guidance UI. Integrated features use backend authentication and APIs.
   Remaining prototype screens retain isolated demo data until connected. */
'use strict';
const UI = (() => {
 const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 const get = id => document.getElementById(id);
 const read = (key, fallback) => { try { return JSON.parse(sessionStorage.getItem('mfu.'+key)) ?? fallback; } catch {return fallback;} };
 const state = { role:'teacher', page:'dashboard', auth:'login', renewal:1,
   posts:read('posts',projects.map(p=>({postId:p.id,title:p.title,summary:p.description,content:p.description,postType:p.type,eventType:p.type,targetAudience:'ครูแนะแนว',eventStartDate:'2026-09-15',eventEndDate:'2026-09-30',visibleStartDate:'2026-09-01',visibleEndDate:'2026-12-31',publishedAt:'2026-09-01T09:00',isHidden:false}))),
   profile:read('profile',{schoolId:1,prefix:'นางสาว',firstName:'ตัวอย่าง',lastName:'ใจดี',positionTitle:'ครูแนะแนว',email:'counselor@example.com',phone:'053916000',activeAcademicYear:2569}),
   registrations:read('registrations',[]),pins:read('pins',[]),deletedTeacher:read('deletedTeacher',false),datasets:read('datasets',[]) };
 const save = key => {try{sessionStorage.setItem('mfu.'+key+(key==='pins'?'.school-'+(state.profile.schoolId||1):''),JSON.stringify(state[key]));}catch{toast('พื้นที่จัดเก็บในแท็บไม่เพียงพอ กรุณาเลือกรูปขนาดเล็กลง');}};
 // Entity-aligned fixtures. Values/codes are examples, not backend enums.
 const schools=[{schoolId:1,name:'โรงเรียนสามัคคีวิทยาคม',schoolCode:'57010001',schoolType:'รัฐบาล',subdistrict:{subdistrictId:1,name:'เวียง',zipcode:'57000',district:{districtId:1,name:'เมืองเชียงราย',province:{provinceId:1,name:'เชียงราย',region:'เหนือ'}}}},{schoolId:2,name:'โรงเรียนยุพราชวิทยาลัย',schoolCode:'50010002',schoolType:'รัฐบาล',subdistrict:{subdistrictId:2,name:'ศรีภูมิ',zipcode:'50200',district:{districtId:2,name:'เมืองเชียงใหม่',province:{provinceId:2,name:'เชียงใหม่',region:'เหนือ'}}}}];
 const entryGroups=['รอบที่ 1 Portfolio','รอบที่ 2 Quota','รอบที่ 3 Admission','รอบที่ 4 Direct Admission'].map((entrygroupName,i)=>({entrygroupId:i+1,academicYear:2569,entrygroupName,applyStartDate:`2026-${String(i+8).padStart(2,'0')}-01`,applyEndDate:`2026-${String(i+8).padStart(2,'0')}-20`,resultAnnounceDate:`2026-${String(i+8).padStart(2,'0')}-25`,rightsConfirmDate:`2026-${String(i+8).padStart(2,'0')}-28`}));
 const programs=['วิศวกรรมซอฟต์แวร์','ภาษาอังกฤษ','บริหารธุรกิจ','พยาบาลศาสตร์','เทคโนโลยีดิจิทัล'].map((programName,i)=>({programId:i+1,programName,faculty:i===0?'สำนักวิชาเทคโนโลยีสารสนเทศ':'สำนักวิชา'+['เทคโนโลยีสารสนเทศ','ศิลปศาสตร์','การจัดการ','พยาบาลศาสตร์','เทคโนโลยีสารสนเทศ'][i],entryGroup:entryGroups[0]}));
 const statuses=[['10','สมัครเข้าศึกษา'],['20','มีสิทธิ์สอบสัมภาษณ์'],...Array.from({length:8},(_,i)=>[String(21+i),'สอบข้อเขียน/สัมภาษณ์ รอบ '+(i+1)]),['40','ได้รับการคัดเลือก'],['41','ได้รับการคัดเลือก – ไม่ยืนยันทะเบียน'],['42','ขึ้นทะเบียน – สละสิทธิ์'],['43','ยกเลิกสละสิทธิ์'],['50','ชำระค่าลงทะเบียนแล้ว']].map(([statusCode,description])=>({statusCode,description}));
 const applicants=Array.from({length:60},(_,i)=>({applicantId:i+1,school:schools[i%2],nationalId:'000000000'+String(i+1).padStart(4,'0'),firstName:'นักเรียน '+String(i+1).padStart(3,'0'),lastName:'ข้อมูลสาธิต',academicYear:2569,term:1}));
 const selections=applicants.map((applicant,i)=>({applicationId:i+1,applicant,entryGroup:entryGroups[Math.floor(i/2)%4],quota:{quotaId:i%2+1,quotaName:i%2?'โครงการพิเศษ':'โควตาทั่วไป',faculty:programs[i%5].faculty},status:statuses[Math.floor(i/2)%statuses.length],appliedAt:`2026-10-${String(i%28+1).padStart(2,'0')}T09:00:00`,admin:null,publishedAt:null}));
 const interests=applicants.flatMap((applicant,i)=>Array.from({length:i%3+1},(_,j)=>({interestId:i*3+j+1,applicant,program:programs[(i+j)%5],interestedAt:'2026-10-01T10:00:00'})));
 // The server must enforce this same school scope before returning data.
 const ownSchool=()=>schools.find(s=>s.schoolId===(state.profile.schoolId||1))||schools[0];
 const scope=list=>state.role==='admin'?list:list.filter(x=>(x.applicant||x).school?.schoolId===ownSchool().schoolId);
 const demoNote='';
 const projectTitles=['โครงการพัฒนาทักษะภาษาอังกฤษสำหรับนักศึกษา','โครงการฝึกอบรมการเขียนโปรแกรม Python','โครงการพัฒนาทักษะการเป็นผู้ประกอบการ','โครงการฝึกอบรมการวิเคราะห์ข้อมูลด้วย Tableau','โครงการพัฒนาทักษะการวิจัยเชิงประยุกต์'];
 const PIN='<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true"><path d="m16 3 5 5-4 1-3 5v3l-3-3-6 6 6-6-3-3h3l5-3z"/></svg>';
 const pinButton=id=>`<button class="pin-button ${state.pins.includes(id)?'pinned':''}" type="button" data-ui="pin-post" data-id="${id}" aria-label="${state.pins.includes(id)?'ยกเลิกปักหมุด':'ปักหมุดข่าว'}" aria-pressed="${state.pins.includes(id)}" title="${state.pins.includes(id)?'ยกเลิกปักหมุด':'ปักหมุดข่าว'}">${PIN}</button>`;
 const btn=(label,action,cls='btn-outline',extra='')=>`<button type="button" class="btn ${cls}" data-ui="${action}" ${extra}>${label}</button>`;
 const field=(label,name,value='',type='text',required=false,attrs='')=>`<label>${label}${required?' <span class="required">*</span>':''}<input name="${name}" type="${type}" value="${esc(value)}" ${required?'required':''} ${attrs}></label>`;
 const select=(label,name,options)=>`<label>${label}<select name="${name}">${options.map(v=>`<option value="${esc(v)}">${esc(v)}</option>`).join('')}</select></label>`;
 const panel=html=>`<div class="panel pad">${html}</div>`;
 const heading=(title,sub='')=>`<div class="page-heading"><div><h1>${title}</h1><p>${sub}</p></div></div>`;
 function table(headers,rows){return `<div class="table-wrap"><table><thead><tr>${headers.map(x=>`<th>${x}</th>`).join('')}</tr></thead><tbody>${rows.length?rows.map(row=>`<tr>${row.map(cell=>`<td>${cell}</td>`).join('')}</tr>`).join(''):`<tr><td colspan="${headers.length}">ไม่พบข้อมูล</td></tr>`}</tbody></table></div>`;}
 const tag=(s)=>`<span class="tag tag-green">${esc(s)}</span>`;
 function counts(){return [scope(applicants).length,...statuses.map(s=>scope(selections).filter(a=>a.status.statusCode===s.statusCode).length)];}
 function kpis(labels,values){return `<div class="kpi-grid">${labels.map((l,i)=>`<div class="panel kpi"><span>${l}</span><strong>${values[i]}</strong><small>ปีการศึกษา 2569</small></div>`).join('')}</div>`;}
 // Semantic CSS charts need no CDN; every bar includes its exact numeric value.
 function bars(title,labels,values){const max=Math.max(1,...values); return panel(`<h3>${title}</h3><div class="bars" role="img" aria-label="${esc(title+': '+labels.map((l,i)=>l+' '+values[i]).join(', '))}">${labels.map((l,i)=>`<div class="bar-row"><span>${esc(l)}</span><div class="bar-track"><i style="width:${values[i]/max*100}%"></i></div><strong>${values[i]}</strong></div>`).join('')}</div>`);}
 function donut(title,labels,values){const total=values.reduce((a,b)=>a+b,0);let acc=0;const colors=['#7A2222','#C9A227','#D97706','#15803D'];return panel(`<h3>${title}</h3><div class="donut-layout"><div class="donut" style="background:conic-gradient(${values.map((v,i)=>{const start=acc;acc+=total?v/total*100:0;return `${colors[i%4]} ${start}% ${acc}%`;}).join(',')})"><strong>${total}<small>รายการ</small></strong></div><div>${labels.map((l,i)=>`<p><i class="dot" style="background:${colors[i%4]}"></i>${esc(l)} <b>${values[i]}</b></p>`).join('')}</div></div>`);}
 function legacyStats(){return kpis(['ผู้สมัครทั้งหมด','รอตรวจสอบ','ผ่านการคัดเลือก','ยืนยันสิทธิ์'],counts())+`<div class="chart-grid">${bars('จำนวนผู้สนใจแยกตามหลักสูตร',programs.map(p=>p.programName),programs.map(p=>interests.filter(a=>a.program.programId===p.programId).length))}${donut('สถานะการสมัคร',statuses.map(s=>s.description),statuses.map(s=>selections.filter(a=>a.status.statusCode===s.statusCode).length))}</div>`;}
 const teacherMenu=[['dashboard','⌂','แดชบอร์ด'],['projects','▤','โครงการสำหรับครูแนะแนว'],['calendar','▦','ปฏิทินกำหนดการ'],['applicants','⌕','ค้นหาผู้สมัคร'],['overview','▥','ภาพรวม'],['schools','▦','จัดการข้อมูลโรงเรียน'],['profile','▣','ข้อมูลส่วนตัวของฉัน']];
 const adminMenu=[['admin-dashboard','⌂','แดชบอร์ด'],['posts','▤','ข่าวสารและประชาสัมพันธ์'],['results','▧','ผลการรับสมัคร'],['post-new','▣','สร้างเนื้อหา PR'],['analysis','▥','วิเคราะห์ความสนใจหลักสูตร'],['reports','◷','รายงานภาพรวมระดับระบบ'],['school-search','▦','ค้นหาข้อมูลโรงเรียน']];
 function navIcon(page){const paths={posts:'M5 2h10l4 4v16H5zM8 10h8M8 14h6M8 18h4M15 2v5h4',results:'M3 3h16v11M3 8h10M3 13h5M3 3v18h8M21 21l-4-4M18 13a4 4 0 1 1-8 0 4 4 0 0 1 8 0','post-new':'M3 4h18v16H3zM6 16l4-5 4 3 3-4 3 6M9 8h.01',analysis:'M3 8h18v13H3zM5 8V4h14v4M3 13h18M10 12h4',reports:'M12 3v9h9M9 3a9 9 0 1 0 12 12','school-search':'M3 21V9h8v12M13 21V4h8v17M6 12h2M6 16h2M16 7h2M16 11h2M16 15h2M1 21h22',dashboard:'M3 11 12 3l9 8v10h-6v-6H9v6H3z','admin-dashboard':'M3 11 12 3l9 8v10h-6v-6H9v6H3z',calendar:'M4 5h16v16H4zM8 2v6M16 2v6M4 10h16',applicants:'M3 3h16v10M3 8h12M3 13h6M3 3v18h8M20 20l-4-4M17 13a4 4 0 1 1-8 0 4 4 0 0 1 8 0',profile:'M4 3h16v18H4zM9 15h6M8 18h8M14 8a2 2 0 1 1-4 0 2 2 0 0 1 4 0',schools:'M5 3h14v18H5zM8 6h2M14 6h2M8 10h2M14 10h2M8 14h2M14 14h2M10 21v-4h4v4',projects:'M5 2h9l5 5v14H5zM14 2v6h5M8 12h8M8 16h8',overview:'M3 4h18v16H3zM7 16v-4M12 16V8M17 16v-6'};return `<svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" stroke-linecap="round" aria-hidden="true"><path d="${paths[page]||paths.overview}"/></svg>`;}
 function accountAvatarMarkup() {
    if(state.role==='admin')return 'A';
    const picture=state.profile.profilePicture;
    if(typeof picture==='string'&&/^\/uploads\/profile-pictures\/[a-f0-9-]+\.(?:jpg|png)$/i.test(picture)) {
        return `<img src="${esc(picture)}" alt="รูปโปรไฟล์" style="display:block;width:100%;height:100%;object-fit:cover;border-radius:50%">`;
    }
    return 'ค';
 }
 function shell(){document.body.classList.remove('auth-mode');get('authRoot').classList.add('hidden');document.querySelector('.app-shell').classList.remove('hidden');document.querySelector('.side-nav').innerHTML=(state.role==='admin'?adminMenu:teacherMenu).map(([p,i,l])=>`<button class="nav-link ${p===state.page?'active':''}" data-ui="nav" data-target="${p}"><span class="nav-icon">${navIcon(p)}</span>${l}</button>`).join('');document.querySelector('.app-shell').classList.toggle('admin-ui',state.role==='admin');document.querySelector('.sidebar-user').innerHTML=`<button class="account-trigger" type="button" data-ui="account-menu" aria-haspopup="dialog" aria-label="เปิดเมนูบัญชี ${state.role==='admin'?'ผู้ดูแลระบบ':esc(state.profile.firstName+' '+state.profile.lastName)}"><span class="avatar">${accountAvatarMarkup()}</span><span class="account-copy"><strong>${state.role==='admin'?'ผู้ดูแลระบบ':esc(state.profile.firstName+' '+state.profile.lastName)}</strong><span>${state.role==='admin'?'แอดมิน':'ครูแนะแนว'}</span></span><span class="account-chevron" aria-hidden="true">⌃</span></button>`;}
 function render(page=state.page){if(page==='publish')page='results';if(state.role==='admin')state.adminFilters={};if(state.role==='teacher'&&!teacherMenu.some(x=>x[0]===page)&&!['renew','project-detail','own-school-stats'].includes(page))page='dashboard';state.page=page;shell();document.querySelectorAll('.page').forEach(el=>el.classList.add('hidden'));const root=get('extendedPage');root.dataset.view=page;root.classList.remove('hidden');root.innerHTML=content(page);document.querySelector('.topbar strong').innerHTML='MFU'+(state.role==='teacher'?`<small class="header-school">${esc(ownSchool().name)}</small>`:'');if(page==='school-search'&&state.role==='admin'&&!state.adminSchoolLoaded&&!state.adminSchoolLoading)void loadAdminSchools();if(page==='admin-dashboard'&&state.role==='admin'&&!state.adminDashboardLoaded&&!state.adminDashboardLoading)void loadAdminDashboard();if(page==='results'&&state.role==='admin'&&!state.adminResultsLoaded&&!state.adminResultsLoading)void loadAdminResults();if(state.role==='admin'&&['analysis','reports'].includes(page)&&!state.liveAdminReportsLoaded&&!state.liveAdminReportsLoading)void loadLiveAdminReports();if(page==='schools'&&state.role==='teacher'&&!state.teacherSchoolLoaded&&!state.teacherSchoolLoading)void loadTeacherSchool();if(page==='overview'&&state.role==='teacher'&&!state.teacherReportLoaded&&!state.teacherReportLoading)void loadTeacherOverviewReport();if(page==='applicants'&&state.role==='teacher'&&!state.teacherApplicantLoaded&&!state.teacherApplicantLoading)void loadTeacherApplicantSearch();if(page==='calendar')calendar();document.querySelectorAll('.nav-link').forEach(b=>b.classList.toggle('active',((b.dataset.target===page&&!(state.role==='admin'&&page==='post-new'&&state.editingPostId))||(state.role==='admin'&&((['admin-applicants'].includes(page)&&b.dataset.target==='results')||(page==='expired'&&b.dataset.target==='posts')||(page==='post-new'&&state.editingPostId&&b.dataset.target==='posts')||(page==='admin-students'&&b.dataset.target==='school-search')))||(page==='renew'&&b.dataset.target==='profile')||(page==='own-school-stats'&&b.dataset.target==='applicants'))));get('sidebar').classList.remove('open');window.scrollTo(0,0);}
function postCards(source = state.posts) {
    const posts = source
        .filter(post => !post.isHidden)
        .sort((a, b) =>
            Number(state.pins.includes(b.postId)) -
            Number(state.pins.includes(a.postId))
        );

    if (posts.length === 0) {
        return `
            <div class="empty-state" role="status">
                <strong>ยังไม่มีข่าวสารหรือประชาสัมพันธ์ในขณะนี้</strong>
                <p>เมื่อมีข่าวสารที่เผยแพร่และอยู่ในช่วงแสดงผล จะแสดงที่นี่</p>
            </div>
        `;
    }

    return `
        <div class="project-grid">
            ${posts.map(post => `
                <article class="project-card">
                    <div class="card-title-row">
                        <div class="tag-row">
                            ${tag(post.postType)}
                            ${tag('ประชาสัมพันธ์')}
                        </div>
                        ${pinButton(post.postId)}
                    </div>
                    <h2>${esc(post.title)}</h2>
                    <p class="description">${esc(post.summary)}</p>
                    <div class="project-meta">
                        วันที่ประกาศ:
                        ${esc(post.publishedAt?.slice(0, 10) || '')}
                        <strong class="project-period">
                            ช่วงประชาสัมพันธ์
                            ${esc(post.visibleStartDate)} –
                            ${esc(post.visibleEndDate)}
                        </strong>
                        ${btn(
                            'อ่านรายละเอียดเพิ่มเติม',
                            'post-detail',
                            'btn-primary btn-block',
                            `data-id="${post.postId}"`
                        )}
                    </div>
                </article>
            `).join('')}
        </div>
    `;
} function content(page){if(state.role==='admin'&&adminPageIds.includes(page))return adminContent(page);switch(page){
 case 'dashboard':return heading('แดชบอร์ด','ข่าวสารและประชาสัมพันธ์สำหรับครูแนะแนว')+panel(postCards());
case 'projects':
    return heading('โครงการสำหรับครูแนะแนว') + panel(`
        <form id="postFilter" class="filter-panel">
            <label>
                ประเภท
                <select name="type">
                    <option value="">ทั้งหมด</option>
                    <option value="NEWS">ข่าวสาร</option>
                    <option value="PR">ประชาสัมพันธ์</option>
                    <option value="ANNOUNCEMENT">ประกาศ</option>
                </select>
            </label>
            ${field('ค้นหา', 'query')}
            <button class="btn btn-primary">ค้นหา</button>
        </form>
        <div id="filteredPosts">${postCards()}</div>
    `); case 'calendar':return calendarPage();
 case 'applicants':return state.searchTab==='schools'?schoolLookup():applicantsPage();
 case 'unused-applicants':return heading('ค้นหาข้อมูลผู้สมัคร')+stats()+panel(`<form id="applicantFilter" class="filter-panel">${field('ชื่อ / รหัสผู้สมัคร','query')}${select('ปีการศึกษา','year',['2569','2568'])}${select('รอบสมัคร','round',['ทุกรอบ',...entryGroups.map(g=>g.entrygroupName)])}${select('สถานะ','status',['ทั้งหมด',...statuses.map(s=>s.description)])}<button class="btn btn-primary">ค้นหา</button>${btn('ส่งออก CSV','export-applicants')}</form><div id="selectionRows">${selectionTable(scope(selections))}</div>`);
 case 'overview':return teacherReportPage();case 'analysis':case 'reports': return reportPage(page);
 case 'profile':return profilePage();
 case 'schools':return schoolPage();case 'own-school-stats':return ownSchoolStats();
 case 'renew':return renewal();
 case 'admin-dashboard':return heading('สถานะงานเนื้อหาและผลการรับสมัคร')+kpis(['ข่าวทั้งหมด','แสดงบนเว็บไซต์','ซ่อนข่าว','ชุดข้อมูลนำเข้า'],[state.posts.length,state.posts.filter(p=>!p.isHidden).length,state.posts.filter(p=>p.isHidden).length,state.datasets.length])+`<div class="chart-grid">${bars('สถานะเนื้อหา', ['แสดงบนเว็บไซต์','ซ่อนข่าว'],[state.posts.filter(p=>!p.isHidden).length,state.posts.filter(p=>p.isHidden).length])}${donut('สถานะผู้สมัคร',statuses.map(s=>s.description),counts().slice(1))}</div>`+panel(`<h3>รายการอัปเดตล่าสุด</h3>${postTable(state.posts)}`);
 case 'posts':case 'expired':return heading(page==='expired'?'รายการเนื้อหาหมดอายุ':'จัดการข่าวสารและประชาสัมพันธ์')+panel(`<div class="actions">${btn('+ สร้างเนื้อหาใหม่','nav','btn-primary','data-target="post-new"')}${btn('รายการหมดอายุ','nav','btn-outline','data-target="expired"')}</div><form id="adminPostFilter" class="filter-panel">${field('ค้นหา','query')}${select('การแสดงผล','visible',['ทั้งหมด','แสดง','ซ่อน'])}<button class="btn btn-primary">ค้นหา</button></form><div id="postRows">${postTable(page==='expired'?state.posts.filter(p=>p.visibleEndDate && p.visibleEndDate<new Date().toISOString().slice(0,10)):state.posts)}</div>`);
 case 'post-new':return postForm();
 case 'results':return adminContent('results');
 case 'school-search':return heading('ค้นหาข้อมูลโรงเรียน')+kpis(['โรงเรียนในข้อมูลตัวอย่าง','มีบัญชีครูแนะแนว'],[schools.length,state.deletedTeacher?0:1])+panel(`<form id="schoolFilter" class="filter-panel">${field('ชื่อโรงเรียน','query')}${select('จังหวัด','province',['ทุกจังหวัด','เชียงราย','เชียงใหม่'])}<button class="btn btn-primary">ค้นหา</button></form><div id="schoolRows">${schoolTable(schools)}</div>`);
 default:return heading('ไม่พบหน้าที่ต้องการ');
 }}
 function selectionTable(list){list=scope(list);return table(['รหัสผู้สมัคร','ชื่อ-นามสกุล','โรงเรียน','ปีการศึกษา','รอบ / โควตา','สถานะ','รายละเอียด'],list.map(s=>[s.applicant.applicantId,esc(s.applicant.firstName+' '+s.applicant.lastName),esc(s.applicant.school.name),s.applicant.academicYear,esc(s.entryGroup.entrygroupName+' / '+s.quota.quotaName),tag(s.status.description),btn('ดูรายละเอียด','applicant-detail','btn-outline',`data-id="${s.applicationId}"`)]));}
 function postTable(list){return table(['ชื่อเรื่อง','ประเภท','ช่วงแสดงข่าว','การแสดงผล','การจัดการ'],list.map(p=>[esc(p.title),esc(p.postType),esc(p.visibleStartDate+' – '+p.visibleEndDate),tag(p.isHidden?'ซ่อน':'แสดง'),btn('รายละเอียด','post-detail','btn-outline',`data-id="${p.postId}"`)+btn('แก้ไข','post-edit','btn-outline',`data-id="${p.postId}"`)]));}
 function schoolTable(list){return table(['ชื่อโรงเรียน','จังหวัด','ผู้สมัคร','บัญชีครูแนะแนว','รายละเอียด'],list.map(s=>[esc(s.name),esc(s.subdistrict.district.province.name),applicants.filter(a=>a.school.schoolId===s.schoolId).length,(state.role==='admin'?adminBadge(s.schoolId===1&&!state.deletedTeacher?'มีบัญชี':'ไม่มีบัญชี'):tag(s.schoolId===1&&!state.deletedTeacher?'มีบัญชี':'ไม่มีบัญชี')),btn('รายละเอียด','school-detail','btn-outline',`data-id="${s.schoolId}"`)]));}
 function postForm(p={}){if(state.role==='admin')return adminPostForm(p);return heading(p.postId?'แก้ไขข่าวสารและประชาสัมพันธ์':'สร้างและเผยแพร่ประชาสัมพันธ์')+`<div class="detail-layout">${panel(`<form id="postForm" class="form-grid"><input type="hidden" name="postId" value="${p.postId||''}">${select('ประเภทเนื้อหา','postType',[p.postType||'ประชาสัมพันธ์','กิจกรรม','ทุนการศึกษา'])}${select('การแสดงผล','isHidden',[p.isHidden?'ซ่อน':'แสดง',p.isHidden?'แสดง':'ซ่อน'])}<div class="full">${field('ชื่อเรื่อง','title',p.title,'text',true,'maxlength="255"')}</div><label class="full">สรุป<textarea name="summary" rows="3">${esc(p.summary||'')}</textarea></label><label class="full">เนื้อหา<textarea name="content" rows="7" required>${esc(p.content||'')}</textarea></label>${field('กลุ่มเป้าหมาย','targetAudience',p.targetAudience||'ครูแนะแนว')}${field('ประเภทกิจกรรม','eventType',p.eventType)}${field('วันเริ่มกิจกรรม','eventStartDate',p.eventStartDate,'date')}${field('วันสิ้นสุดกิจกรรม','eventEndDate',p.eventEndDate,'date')}${field('วันเริ่มแสดง','visibleStartDate',p.visibleStartDate,'date',true)}${field('วันสิ้นสุดแสดง','visibleEndDate',p.visibleEndDate,'date',true)}<div class="full notice">สถานที่ เอกสารแนบ และสถานะฉบับร่างใน Figma ยังไม่มีฟิลด์ตรงใน Post จึงรอยืนยันสัญญา API</div><div class="form-actions full">${btn('ยกเลิก','nav','btn-outline','data-target="posts"')}${btn('ดูตัวอย่าง','preview-post')}<button class="btn btn-primary">บันทึกในตัวอย่าง</button></div></form>`)}${panel('<h3>ตัวอย่างการแสดงผล</h3><div id="postPreview">กรอกข้อมูลแล้วกดดูตัวอย่าง</div>')}</div>`;}
 function schoolForm(){const s=read('school',ownSchool());return heading('จัดการข้อมูลโรงเรียน')+panel(`<div class="tabs">${btn('ข้อมูลทั่วไป','school-general')}${btn('ข้อมูลผู้บริหารสถานศึกษา','school-director')}</div><div id="schoolEditor"><form id="schoolForm" class="form-grid">${field('ชื่อโรงเรียน','name',s.name,'text',true,'maxlength="200" readonly')}${field('รหัสโรงเรียน','schoolCode',s.schoolCode,'text',false,'maxlength="10"')}${select('สังกัด / ประเภทโรงเรียน','schoolType',[s.schoolType,'เอกชน'])}${select('จังหวัด','province',['เชียงราย','เชียงใหม่'])}${select('อำเภอ','district',['เมืองเชียงราย'])}${select('ตำบล','subdistrict',['เวียง'])}${field('รหัสไปรษณีย์','zipcode',s.subdistrict.zipcode,'text',false,'readonly')}<div class="full notice">ตัวเลือกที่ตั้งเป็นชุดตัวอย่าง รอ API รายการจังหวัด อำเภอ และตำบล</div><div class="form-actions full"><button class="btn btn-primary">บันทึกข้อมูลตัวอย่าง</button></div></form></div>`);}
 // Login must succeed on the server; role selectors are never credentials.
 let loginPending=false, loginGeneration=0;
 function jwtLoginRole(value){
   try {
     const parts=value.split('.');if(parts.length!==3)throw Error();
     const payload=parts[1].replace(/-/g,'+').replace(/_/g,'/');
     const claims=JSON.parse(atob(payload.padEnd(Math.ceil(payload.length/4)*4,'=')));
     if(!['ADMIN','TEACHER'].includes(claims.role)||!claims.sub||!Number.isFinite(claims.exp)||claims.exp*1000<=Date.now())throw Error();
     return claims.role;
   }catch{throw new Error('Backend ส่ง Token ที่ไม่มีบทบาทหรือหมดอายุแล้ว');}
 }
async function backendLogin(form, data) {
    if (loginPending) return;

    const api = window.MfuApi;
    if (!api?.login) {
        toast('ไม่พบ API adapter กรุณาตรวจว่าโหลด api.js ก่อน ui.js');
        return;
    }

    const attempt = ++loginGeneration;
    const chosenRole = authRole;
    const button = form.querySelector(
        'button[type="submit"],button.btn-primary'
    );
    const label = button?.textContent;

    loginPending = true;
    if (button) {
        button.disabled = true;
        button.textContent = 'กำลังเข้าสู่ระบบ…';
    }

    const current = () =>
        attempt === loginGeneration &&
        get('authForm') === form &&
        state.auth === 'login';

    try {
        const result = await api.login(data.email.trim(), data.password);
        if (!current()) return;

        const role = jwtLoginRole(result.token);
        const expectedRole = chosenRole === 'admin' ? 'ADMIN' : 'TEACHER';

        if (role !== expectedRole) {
            throw new Error(
                'บัญชีนี้ไม่ตรงกับหน้าที่เลือก กรุณาเลือกหน้าล็อกอินให้ตรงกับบทบาทของบัญชี'
            );
        }

        let profile;
        let posts;

        if (role === 'TEACHER') {
            profile = await api.request(
                'GuidanceTeacherController.getMyProfile'
            );
            if (!current()) return;

            if (
                !profile?.teacherId ||
                !profile.school?.schoolId ||
                !profile.school.name
            ) {
                throw new Error('ข้อมูลบัญชีหรือโรงเรียนจาก Backend ไม่ครบ');
            }

            posts = await api.request('PostController.getAll');
            if (!current()) return;

            if (!Array.isArray(posts)) {
                throw new Error('รูปแบบข้อมูลข่าวสารจาก Backend ไม่ถูกต้อง');
            }
        } else {
    if (!current()) return;

    const adminPosts = await api.request('PostController.getAllForAdmin');
    if (!current()) return;

    if (!Array.isArray(adminPosts)) {
        throw new Error('รูปแบบข้อมูลข่าวสารจาก Backend ไม่ถูกต้อง');
    }

    state.posts = adminPosts.map(post => ({
        ...post,
        uiDraft: post.publishedAt == null
    }));
}
const calendarData = await api.request(
    'CalendarController.getCalendarEvents'
);
if (!current()) return;

if (!Array.isArray(calendarData)) {
    throw new Error('รูปแบบข้อมูลปฏิทินจาก Backend ไม่ถูกต้อง');
}

state.calendarApiEvents = calendarData;
if (role === 'TEACHER') {
    const applicantData = await api.request(
        'ApplicantController.searchStudents',
        {
            query: {
                page: 0,
                size: 5
            }
        }
    );

    if (!current()) return;

    if (!Array.isArray(applicantData?.content) ||
        !Number.isInteger(applicantData.currentPage) ||
        !Number.isInteger(applicantData.totalPages) ||
        !Number.isInteger(applicantData.totalItems)) {
        throw new Error('รูปแบบข้อมูลผู้สมัครจาก Backend ไม่ถูกต้อง');
    }

    state.applicantSearchResult = applicantData;
    state.applicantSearchKeyword = '';

    state.profilePrefixes = [];
state.profilePrefixId = profile.prefix?.prefixId ?? null;

try {
    const prefixes = await api.request('PrefixController.getAll');
    if (!current()) return;

    if (!Array.isArray(prefixes) || !prefixes.every(prefix =>
        Number.isInteger(prefix.prefixId) &&
        prefix.prefixId > 0 &&
        typeof prefix.prefixName === 'string' &&
        prefix.prefixName.trim()
    )) {
        throw new Error('รูปแบบข้อมูลคำนำหน้าจาก Backend ไม่ถูกต้อง');
    }

    state.profilePrefixes = prefixes;
} catch (error) {
    if (!current()) return;

    if (error.status === 401 || error.status === 403) {
        throw error;
    }

    console.warn('Unable to load prefixes:', error.message);
}

    const overview = await api.request(
    'ApplicantOverviewReportController.getReport'
);
if (!current()) return;

const overviewFields = [
    'totalApplicants',
    'eligibleForExamination',
    'passedSelection',
    'rightsConfirmed',
    'rightsWaived',
    'enrolled'
];

if (!overview || !overviewFields.every(key =>
    Number.isInteger(overview[key]) && overview[key] >= 0
)) {
    throw new Error('รูปแบบข้อมูลสถิติผู้สมัครจาก Backend ไม่ถูกต้อง');
}

// Store only the totals needed by the statistic cards.
state.applicantOverview = Object.fromEntries(
    overviewFields.map(key => [key, overview[key]])
);

state.applicantRoundStatistics = null;

try {
    // Clear the previous account's program-interest statistics.
    state.applicantProgramStatistics = null;
    const schoolStatistics = await api.request(
        'SchoolStatisticsController.getStatistics'
    );
    if (!current()) return;

    if (!Array.isArray(schoolStatistics?.applicants) ||
        !Number.isInteger(schoolStatistics.academicYear)) {
        throw new Error('รูปแบบข้อมูลสถิติรอบสมัครจาก Backend ไม่ถูกต้อง');
    }

    const roundCounts = new Map();

    for (const applicant of schoolStatistics.applicants) {
        const round =
            applicant.admissionRound || 'ยังไม่มีผลคัดเลือกที่เผยแพร่';

        roundCounts.set(round, (roundCounts.get(round) || 0) + 1);
    }

    state.applicantRoundStatistics = {
        academicYear: schoolStatistics.academicYear,
        rounds: Array.from(roundCounts, ([label, count]) => ({
            label,
            count
        }))
    };
} catch (error) {
    if (!current()) return;

    // Authentication failures must still stop the login flow.
    if (error.status === 401 || error.status === 403) {
        throw error;
    }

    console.warn(
        'Unable to load admission-round statistics:',
        error.message
    );
}

state.applicantProgramStatistics = null;

try {
    const academicYear = profile.activeAcademicYear;

    if (!Number.isInteger(academicYear) || academicYear < 1) {
        throw new Error('ยังไม่ได้กำหนดปีการศึกษาสำหรับบัญชีนี้');
    }

    const report = await api.request(
        'TeacherProgramInterestStatisticsController.getStatistics',
        { query: { academicYear } }
    );
    if (!current()) return;

    const totals = [
        report?.numberOfApplicants,
        report?.numberOfPrograms,
        report?.totalInterests
    ];

    if (!totals.every(value =>
        Number.isInteger(value) && value >= 0
    ) || !Array.isArray(report?.programStatistics)) {
        throw new Error('รูปแบบข้อมูลความสนใจหลักสูตรไม่ถูกต้อง');
    }

    if (!report.programStatistics.every(item =>
        Number.isInteger(item.programId) &&
        typeof item.programName === 'string' &&
        Number.isInteger(item.interestCount) &&
        item.interestCount >= 0
    )) {
        throw new Error('รายละเอียดสถิติหลักสูตรไม่ถูกต้อง');
    }

    state.applicantProgramStatistics = {
        academicYear,
        numberOfApplicants: report.numberOfApplicants,
        numberOfPrograms: report.numberOfPrograms,
        totalInterests: report.totalInterests,
        programs: [...report.programStatistics].sort((a, b) =>
            b.interestCount - a.interestCount ||
            a.programId - b.programId
        )
    };
} catch (error) {
    if (!current()) return;

    if (error.status === 401 || error.status === 403) {
        throw error;
    }

    console.warn(
        'Unable to load program-interest statistics:',
        error.message
    );
}

}
        if (profile) {
            state.profile = {
                ...profile,
                schoolId: profile.school.schoolId,
                prefix: profile.prefix?.prefixName || profile.prefix?.name || '',
                positionTitle: 'ครูแนะแนว'
            };

            const index = schools.findIndex(
                school => school.schoolId === profile.school.schoolId
            );

            if (index < 0) {
                schools.push(profile.school);
            } else {
                schools[index] = profile.school;
            }

            sessionStorage.removeItem('mfu.school');

            // Use the API response, including an empty array.
            state.posts = posts;
        }

        state.role = role === 'ADMIN' ? 'admin' : 'teacher';
        form.elements.password.value = '';
        render(state.role === 'admin' ? 'admin-dashboard' : 'dashboard');
    } catch (error) {
        if (!current()) return;

        api.logout();

        const message =
            error.status === 401
                ? 'อีเมลหรือรหัสผ่านไม่ถูกต้อง หรือบัญชียังไม่ยืนยันอีเมล'
                : error.status === 403
                ? 'บัญชีนี้ไม่มีสิทธิ์เข้าใช้งานส่วนนี้'
                : error instanceof TypeError
                ? 'ติดต่อ Backend ไม่ได้ กรุณาตรวจว่าเซิร์ฟเวอร์กำลังรัน และเปิดเว็บจากพอร์ต Backend'
                : error.message || 'เข้าสู่ระบบไม่สำเร็จ';

        toast(message);
    } finally {
        if (attempt === loginGeneration) {
            loginPending = false;
        }

        if (button && button.isConnected) {
            button.disabled = false;
            button.textContent = label;
        }
    }
}
 let authRole='teacher';
 function auth(view='login',role=authRole){teacherApplicantRequest++;state.teacherApplicantLoaded=false;state.teacherApplicantLoading=false;state.teacherApplicantReport=null;state.teacherApplicantCatalog=[];state.teacherApplicantFilters={};if(state.schoolLogoUrl){URL.revokeObjectURL(state.schoolLogoUrl);state.schoolLogoUrl=null;}state.teacherSchoolRefs=null;teacherReportRequest++;state.teacherReportLoaded=false;state.teacherReportLoading=false;state.teacherReportData=null;state.teacherReportPrograms=[];state.originalReportFilters={};teacherSchoolRequest++;teacherSchoolSaving=false;state.teacherSchoolLoaded=false;state.teacherSchoolLoading=false;state.teacherSchool=null;state.teacherSchoolTab='general';liveAdminReportRequest++;state.liveAdminReportsLoaded=false;state.liveAdminReportsLoading=false;state.liveAdminReportsData=null;state.liveAdminReportFilters={analysis:{},reports:{}};adminResultsRequest++;state.adminResultsLoaded=false;state.adminResultsLoading=false;state.adminResultsRows=[];state.adminResultsView='groups';state.adminResultGroupFilters={};state.adminResultFilters={};state.adminResultsPage=0;adminDashboardRequest++;state.adminDashboardLoaded=false;state.adminDashboardLoading=false;state.adminDashboardReport=null;state.dashboardFilters={};state.adminSchoolLoaded=false;state.adminSchoolLoading=false;state.adminSchoolReport=null;state.adminSchoolFilters={};state.adminSchoolProvinces=[];adminSchoolRequest++;stopRegistrationCheck();loginGeneration++;loginPending=false;window.MfuApi?.logout();if(['register','password','sent','confirmed'].includes(view)){authRole='teacher';state.auth=view;referenceAuth(view);if(view==='sent')startRegistrationCheck();return;}get('authRoot').classList.remove('auth-reference');get('authRoot').removeAttribute('data-auth-step');authRole=role;state.auth=view;document.body.classList.add('auth-mode');document.querySelector('.app-shell').classList.add('hidden');get('authRoot').classList.remove('hidden');const titles={login:role==='admin'?'ระบบสำหรับผู้ดูแลระบบ (Admin)':'ระบบสำหรับครูแนะแนว มหาวิทยาลัยแม่ฟ้าหลวง',register:'ลงทะเบียนขอเข้าใช้งานระบบครูแนะแนว',reset:'ขอรับรหัสผ่านใหม่',password:'ตั้งรหัสผ่านเพื่อเข้าใช้งานระบบ',sent:'ส่งคำขอตัวอย่างสำเร็จ',confirmed:'ตัวอย่างหน้ายืนยันอีเมล'};get('authRoot').innerHTML=`<button class="btn btn-outline auth-switch" data-ui="switch-role">${role==='teacher'?'เข้าสู่ระบบสำหรับแอดมิน':'เข้าสู่ระบบสำหรับครูแนะแนว'}</button><section class="auth-card"><div class="auth-brand"><img src="assets/crest.png" alt="ตรามหาวิทยาลัยแม่ฟ้าหลวง"><p>มหาวิทยาลัยแม่ฟ้าหลวง<br><small>MAE FAH LUANG UNIVERSITY</small></p></div><h1>${titles[view]}</h1><p class="auth-subtitle">เข้าสู่ระบบเพื่อจัดการข้อมูลโควตาและประสานงานเครือข่าย</p>${view==='sent'?`<p>ในระบบจริงจะส่งลิงก์ไปยังอีเมล แต่โหมดนี้ไม่ได้ส่งอีเมล</p>${btn('ดูตัวอย่างหน้ายืนยันอีเมล','auth-confirm','btn-primary btn-block')}`:view==='confirmed'?`<div class="success-state">✓ หน้าตัวอย่างยืนยันอีเมล</div><p>ยังไม่มีการยืนยันบัญชีจริง</p>${btn('กลับเข้าสู่ระบบ','auth-login','btn-primary btn-block')}`:`<form id="authForm" class="form-grid">${view==='register'?select('จังหวัดโรงเรียน','province',['เชียงราย','เชียงใหม่'])+select('โรงเรียน','school',schools.filter(s=>s.subdistrict.district.province.name==='เชียงราย').map(s=>s.name))+select('คำนำหน้า','prefix',['นางสาว','นาง','นาย'])+field('ชื่อ','firstName','','text',true)+field('นามสกุล','lastName','','text',true)+field('ตำแหน่ง','positionTitle','','text',true)+field('โทรศัพท์','phone','','tel',true):''}${view!=='password'?`<div class="full">${field('อีเมล','email','','email',true)}</div>`:''}${view==='login'||view==='password'?`<div class="full password-field">${field('รหัสผ่าน','password','','password',true,'minlength="8" autocomplete="'+(view==='password'?'new-password':'current-password')+'"')}${btn(
  '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>',
  'toggle-password',
  'btn-outline',
  'aria-label="แสดงหรือซ่อนรหัสผ่าน"'
)}</div>`:''}${view==='password'?`<div class="full">${field('ยืนยันรหัสผ่าน','confirm','','password',true,'minlength="8"')}</div><label class="check full"><input type="checkbox" required> ยอมรับเงื่อนไขและยินยอมตามนโยบาย PDPA</label>`:''}<button class="btn btn-primary full">${view==='login'?'เข้าสู่ระบบ':view==='register'?'ยื่นคำขอลงทะเบียน':view==='password'?'ยืนยันและยื่นคำขอ':'ยืนยันคำขอตัวอย่าง'}</button></form><div class="auth-links">${view==='login'?btn('ลืมรหัสผ่าน?','auth-reset'):btn('กลับเข้าสู่ระบบ','auth-login')}</div>`}<p class="auth-help">หากต้องการความช่วยเหลือ ติดต่อส่วนรับนักศึกษา<br>โทร. 0 5391 6103</p></section><footer>© 2026 Mae Fah Luang University. All rights reserved.<br>ส่วนรับนักศึกษา มหาวิทยาลัยแม่ฟ้าหลวง พัฒนาเพื่อการประสานงานโรงเรียนเครือข่ายครูแนะแนว</footer>`;if(view==='login'){const link=get('authRoot').querySelector('[data-ui="auth-reset"]');const label=get('authRoot').querySelector('.password-field label');if(link&&label){label.appendChild(link);link.classList.add('forgot-link');}get('authRoot').querySelector('.auth-links')?.remove();if(role==='teacher'){const line=document.createElement('div');line.className='auth-links';line.innerHTML='<span>ยังไม่มีบัญชี? </span>'+btn('คลิกเพื่อลงทะเบียนครูแนะแนว','auth-register','text-button');get('authForm').after(line);}get('authForm').elements.email.placeholder='example@yourdomain.com';get('authForm').elements.password.placeholder='กรอกรหัสผ่านเพื่อเข้าใช้งาน';}}
 function modal(title,body,actions=''){get('uiDialog').classList.remove('account-dialog','admin-school-drawer');if(state.role==='admin'&&title.includes('โรงเรียน'))get('uiDialog').classList.add('admin-school-drawer');get('uiDialog').innerHTML=`<div class="dialog-head"><h2>${title}</h2>${btn('×','close')}</div>${body}<div class="form-actions">${actions||btn('ปิด','close')}</div>`;get('uiDialog').showModal();}
 let returnFocus;const openModal=(...args)=>{returnFocus=document.activeElement;modal(...args);};
 function download(filename,headers,rows){const encode=v=>'"'+String(v??'').replace(/"/g,'""').replace(/^[=+@\-]/,"'$&")+'"';const csv='\ufeff'+[headers,...rows].map(r=>r.map(encode).join(',')).join('\r\n');const url=URL.createObjectURL(new Blob([csv],{type:'text/csv;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download=filename;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
 let month = new Date();
month = new Date(month.getFullYear(), month.getMonth(), 1);
 function deleteDialog(self){openModal(self?'ลบข้อมูลผู้ใช้?':'ลบบัญชีครูแนะแนว',`<p>ตัวอย่างการลบบัญชี ${esc(state.profile.firstName+' '+state.profile.lastName)}</p><p>ข้อมูลส่วนตัว รูปโปรไฟล์ และสิทธิ์การใช้งานจะถูกนำออกในขั้นตอนจริง</p><div class="notice">โหมดนี้เปลี่ยนเฉพาะข้อมูลจำลองในแท็บนี้</div><label class="check"><input id="deleteConsent" type="checkbox"> ฉันยอมรับเงื่อนไขการลบข้อมูลผู้ใช้</label>`,btn('ยกเลิก','close')+`<button disabled class="btn btn-primary" id="deleteConfirm" data-ui="delete-final" data-self="${self}">ยืนยัน (5)</button>`);let remaining=5;const timer=setInterval(()=>{remaining--;const b=get('deleteConfirm');if(!b||!get('uiDialog').open){clearInterval(timer);return;}b.textContent=remaining>0?`ยืนยัน (${remaining})`:'ยืนยัน';b.dataset.ready=remaining<=0?'true':'false';b.disabled=remaining>0||!get('deleteConsent').checked;if(!remaining)clearInterval(timer);},1000);}
 let emailChangePending = false;
 function openEmailChange() {
    if (state.role !== 'teacher') return;
    openModal('เปลี่ยนอีเมล', `
        <p>อีเมลปัจจุบัน: ${esc(state.profile.email)}</p>
        <p>ระบบจะส่งลิงก์ยืนยันไปยังอีเมลใหม่ อีเมลเข้าสู่ระบบจะเปลี่ยนเมื่อยืนยันสำเร็จเท่านั้น</p>
        ${state.profile.pendingEmail ? `<p>รอยืนยัน: ${esc(state.profile.pendingEmail)}</p>` : ''}
        <form id="emailChangeForm" class="form-grid">
            <label class="full">อีเมลใหม่
                <input type="email" name="newEmail" required maxlength="150" autocomplete="email">
            </label>
            <p id="emailChangeStatus" class="full" role="status" aria-live="polite"></p>
            <button type="submit" class="btn btn-primary full" ${emailChangePending ? 'disabled' : ''}>ส่งลิงก์ยืนยัน</button>
        </form>
    `);
 }
 async function submitEmailChange(form) {
    if (state.role !== 'teacher' || emailChangePending) return;
    const newEmail = form.elements.newEmail.value.trim().toLowerCase();
    const status = form.querySelector('#emailChangeStatus');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newEmail)) {
        status.textContent = 'กรุณากรอกอีเมลให้ถูกต้อง';
        return;
    }
    if (newEmail === String(state.profile.email || '').trim().toLowerCase()) {
        status.textContent = 'กรุณาใช้อีเมลใหม่ที่ต่างจากอีเมลปัจจุบัน';
        return;
    }
    const teacherId = state.profile.teacherId;
    const generation = loginGeneration;
    const button = form.querySelector('button[type="submit"]');
    emailChangePending = true;
    button.disabled = true;
    status.textContent = 'กำลังส่งลิงก์ยืนยัน…';
    const current = () => state.role === 'teacher' &&
        state.profile.teacherId === teacherId && loginGeneration === generation;
    try {
        await window.MfuApi.request('GuidanceTeacherController.requestEmailChange', {
            body: {newEmail}
        });
        if (!current()) return;
        state.profile.pendingEmail = newEmail;
        if (get('emailChangeForm') !== form) return;
        // Do not replace the active login email before verification.
        form.elements.newEmail.disabled = true;
        form.dataset.sent = 'true';
        status.textContent = 'ส่งลิงก์แล้ว กรุณาเปิดอีเมลใหม่และกดลิงก์ยืนยัน จากนั้นออกจากระบบและเข้าสู่ระบบด้วยอีเมลใหม่และรหัสผ่านเดิม';
    } catch (error) {
        if (current() && get('emailChangeForm') === form) {
            status.textContent = error.message || 'ส่งลิงก์ไม่สำเร็จ กรุณาลองอีกครั้ง';
        }
    } finally {
        emailChangePending = false;
        const activeForm = get('emailChangeForm');
        const activeButton = activeForm?.querySelector('button[type="submit"]');
        if (activeButton && activeForm.dataset.sent !== 'true') activeButton.disabled = false;
    }
 }
 document.addEventListener('click', e => {
    if (e.target.closest('[data-ui="change-email"]')) openEmailChange();
 });
 document.addEventListener('submit', e => {
    if (e.target.id !== 'emailChangeForm') return;
    e.preventDefault();
    if (e.target.dataset.sent !== 'true') void submitEmailChange(e.target);
 });
 let profilePictureUpload = null;
 async function uploadProfilePicture(input) {
    const file = input.files?.[0];
    if (!file) return;
    if (state.role !== 'teacher' || !state.profile.teacherId) {
        input.value = '';
        return;
    }
    if (profilePictureUpload) {
        toast('กำลังอัปโหลดรูปภาพ กรุณารอสักครู่');
        input.value = '';
        return;
    }
    if (!['image/png', 'image/jpeg'].includes(file.type) ||
        file.size === 0 || file.size > 2 * 1024 * 1024) {
        toast('กรุณาเลือกรูป JPG/PNG ขนาดไม่เกิน 2 MB');
        input.value = '';
        return;
    }
    const form = get('profileForm');
    if (form?.dataset.saving === 'true') {
        toast('กำลังบันทึกข้อมูล กรุณารอสักครู่');
        input.value = '';
        return;
    }
    const operation = {
        teacherId: state.profile.teacherId,
        generation: loginGeneration
    };
    profilePictureUpload = operation;
    const button = form?.querySelector('button.btn-primary');
    const wasDisabled = button?.disabled;
    input.disabled = true;
    if (button) button.disabled = true;
    const current = () => state.role === 'teacher' &&
        state.profile.teacherId === operation.teacherId &&
        loginGeneration === operation.generation;
    try {
        const body = new FormData();
        body.append('file', file);
        const result = await window.MfuApi.request(
            'GuidanceTeacherController.uploadMyProfilePicture', {body}
        );
        if (!current()) return;
        if (typeof result?.profilePicture !== 'string' ||
            !/^\/uploads\/profile-pictures\/[a-f0-9-]+\.(?:jpg|png)$/i.test(result.profilePicture)) {
            throw new Error('Backend ส่งที่อยู่รูปโปรไฟล์ไม่ถูกต้อง กรุณาโหลดหน้าใหม่');
        }
        state.profile.profilePicture = result.profilePicture;
        delete state.profile.photo;
        // Update just the avatar, preserving unsaved profile form edits.
        const avatar = state.page === 'profile' ? get('profileAvatar') : null;
        if (avatar) {
            avatar.innerHTML = `<img src="${esc(result.profilePicture)}" alt="รูปโปรไฟล์">`;
        }
        document.querySelectorAll('.sidebar-user .avatar, .account-popup .avatar').forEach(element=>{element.innerHTML=accountAvatarMarkup();});
        toast('อัปโหลดและบันทึกรูปโปรไฟล์แล้ว');
    } catch (error) {
        if (current()) toast(error.message || 'อัปโหลดรูปโปรไฟล์ไม่สำเร็จ');
    } finally {
        if (profilePictureUpload === operation) profilePictureUpload = null;
        input.value = '';
        if (input.isConnected) input.disabled = false;
        if (button?.isConnected) button.disabled = wasDisabled;
    }
 }
document.addEventListener('reset', e => {
    if (e.target.id === 'originalReportFilter') {
        state.originalReportFilters = {};
        state.reportListPage=1;
        void loadTeacherOverviewReport();
    }

    if (e.target.id === 'applicantFilter') {
        state.teacherApplicantFilters={};
        state.teacherApplicantPage=0;
        void loadTeacherApplicantSearch();
    }

    if (e.target.id === 'calendarFilter') {
        state.calendarFilters = {};
        setTimeout(calendar, 0);
    }
}); document.addEventListener('change',e=>{if(state.auth==='register'&&e.target.name==='province'&&e.target.closest('#authForm')){const target=get('authForm').elements.schoolId;target.innerHTML=registrationSchoolOptions(e.target.value);target.disabled=!e.target.value;}if(state.role==='admin'&&state.page==='analysis'&&e.target.closest('#adminReportFilter'))get('adminReportFilter').requestSubmit();if(e.target.id==='renewConsent'&&get('renewConfirm'))get('renewConfirm').disabled=renewalPending||!e.target.checked;if(e.target.id==='autoRenew'){sessionStorage.setItem('mfu.autoRenew',JSON.stringify(e.target.checked));}if(e.target.id==='restoredPhoto'){void uploadProfilePicture(e.target);}if(e.target.id==='deleteConsent')get('deleteConfirm').disabled=!e.target.checked||get('deleteConfirm').dataset.ready!=='true';if(e.target.id==='photoInput'){const f=e.target.files[0];if(!f)return;if(!['image/png','image/jpeg'].includes(f.type)||f.size>5*1024*1024){toast('กรุณาเลือก JPG/PNG ไม่เกิน 5 MB');e.target.value='';return;}const url=URL.createObjectURL(f);get('profileAvatar').innerHTML=`<img alt="รูปโปรไฟล์ตัวอย่าง" src="${url}">`;setTimeout(()=>URL.revokeObjectURL(url),5000);}});
 document.addEventListener('click',e=>{const b=e.target.closest('[data-ui]');if(!b)return;const action=b.dataset.ui;switch(action){
 case 'nav':if(b.dataset.target==='post-new')state.editingPostId=null;render(b.dataset.target);calendar();break;
 case 'logout':window.MfuApi?.logout();get('uiDialog').close();auth('login',state.role);break;case 'admin-export-options':if(['analysis','reports'].includes(state.page)&&state.liveAdminReportsData&&!state.liveAdminReportsError){openModal('ดาวน์โหลดรายงาน','<p>ส่งออกข้อมูลตามตัวกรองที่แสดงอยู่</p>',btn('CSV','live-admin-report-csv','btn-primary')+btn('PDF','live-admin-report-pdf','btn-outline'));}break;case 'live-admin-report-csv':exportLiveAdminReport('csv');break;case 'live-admin-report-pdf':exportLiveAdminReport('pdf');break;case 'live-admin-report-retry':void loadLiveAdminReports();break;case 'admin-print':get('uiDialog').close();window.print();break;case 'account-menu':openModal(state.role==='admin'?'บัญชีผู้ดูแลระบบ':esc(userName()),`<div class="account-popup"><div class="avatar">${accountAvatarMarkup()}</div><strong>${state.role==='admin'?'ผู้ดูแลระบบ':esc(userName())}</strong><p>${state.role==='admin'?'แอดมิน':esc(ownSchool().name)}</p></div>`,btn('ปิด','close')+btn('ออกจากระบบ','logout','btn-primary'));get('uiDialog').classList.add('account-dialog');break;
 case 'switch-role':auth('login',authRole==='admin'?'teacher':'admin');break;
 case 'auth-toggle-secret':{const input=get('authForm').elements[b.dataset.input];input.type=input.type==='password'?'text':'password';b.setAttribute('aria-pressed',String(input.type==='text'));b.setAttribute('aria-label',(input.type==='text'?'ซ่อน':'แสดง')+(b.dataset.input==='confirm'?'ยืนยันรหัสผ่าน':'รหัสผ่าน'));break;}case 'registration-back':if(!registrationPending&&!state.passwordResetToken&&state.registrationDraft){auth('register','teacher');}break;case 'registration-consent':openModal('หนังสือแสดงความยินยอม (PDPA)','<p>กรุณาเชื่อมเอกสารนโยบายคุ้มครองข้อมูลส่วนบุคคลฉบับเต็มของระบบในส่วนนี้ก่อนเปิดใช้งานจริง</p>');break;case 'auth-login':auth('login');break;case 'auth-register':void startRegistration();break;case 'registration-retry':void startRegistration();break;case 'auth-reset':auth('reset');break;case 'auth-confirm':auth('confirmed');break;
 case 'toggle-password':{const i=document.querySelector('.password-field input');i.type=i.type==='password'?'text':'password';b.setAttribute('aria-label',i.type==='password'?'แสดงรหัสผ่าน':'ซ่อนรหัสผ่าน');b.setAttribute('aria-pressed',i.type==='text');;break;}
 case 'post-detail':postDetail(b.dataset.id);break;
 case 'post-edit':state.editingPostId=+b.dataset.id;render('post-new');get('extendedPage').innerHTML=demoNote+postForm(state.posts.find(p=>p.postId===+b.dataset.id));break;
 case 'preview-post':{const f=new FormData(get('postForm'));get('postPreview').innerHTML=`${tag(f.get('postType'))}<h2>${esc(f.get('title'))}</h2><p class="preserved">${esc(f.get('summary')||f.get('content'))}</p><p>${esc(f.get('visibleStartDate'))} – ${esc(f.get('visibleEndDate'))}</p>`;break;}
 case 'pin-post':{const id=Number(b.dataset.id||state.selectedPost);state.pins=state.pins.includes(id)?state.pins.filter(x=>x!==id):[...state.pins,id];save('pins');if(state.page==='project-detail')postDetail(id);else render(state.page);break;}
 case 'register-post':return; if(!state.registrations.includes(state.selectedPost)){state.registrations.push(state.selectedPost);save('registrations');}postDetail(state.selectedPost);toast('บันทึกการลงทะเบียนในตัวอย่างแล้ว');break;
 case 'admin-result-open':openResultGroup(Number(b.dataset.round),Number(b.dataset.quota));break;case 'admin-result-back':state.adminResultsView='groups';state.adminResultsPage=state.adminResultGroupPage||0;render('results');break;case 'admin-result-detail':showResultApplicant(Number(b.dataset.id));break;case 'admin-results-retry':void loadAdminResults();break;case 'admin-results-page':state.adminResultsPage=Number(b.dataset.page);render('results');break;case 'admin-dashboard-retry':void loadAdminDashboard();break;case 'admin-schools-page':void loadAdminSchools(Number(b.dataset.page));break;case 'admin-schools-retry':void loadAdminSchools(state.adminSchoolReport?.currentPage||0);break;case 'school-detail':{if(state.role==='admin'){showAdminSchoolDetail(Number(b.dataset.id));break;}const s=schools.find(x=>x.schoolId===+b.dataset.id);if(state.role!=='admin'&&s?.schoolId!==ownSchool().schoolId)return;openModal(esc(s.name),`<p>${esc(s.subdistrict.district.province.name)}</p><h3>ครูแนะแนวประจำโรงเรียน</h3>${s.schoolId===1&&!state.deletedTeacher?`<p>${esc(state.profile.firstName+' '+state.profile.lastName)} · ${esc(state.profile.email)}</p>${btn('ลบบัญชีครูแนะแนว','delete-teacher')}`:'<p>ไม่มีบัญชีครูแนะแนวในชุดตัวอย่าง</p>'}<h3>รายชื่อผู้สมัคร</h3>${selectionTable(selections.filter(a=>a.applicant.school.schoolId===s.schoolId))}`);break;}
 case 'applicant-detail':{const s=scope(selections).find(x=>x.applicationId===+b.dataset.id);if(!s)return;openModal('รายละเอียดผู้สมัคร',`<h3>${esc(s.applicant.firstName+' '+s.applicant.lastName)}</h3><p>รหัส ${s.applicant.applicantId} · เลขประจำตัวประชาชน 0-XXXX-XXXXX-XX-X (สมมติ)</p><p>${esc(s.applicant.school.name)}</p><p>${esc(s.entryGroup.entrygroupName)} · ${esc(s.quota.quotaName)}</p>${tag(s.status.description)}<p>วันสมัคร ${esc(s.appliedAt)}</p>`);break;}
 case 'program-applicants':if(state.role==='admin'){state.adminProgramId=+b.dataset.id;state.adminApplicantPage=1;state.adminFilters={};render('admin-applicants');break;}{const ids=interests.filter(i=>i.program.programId===+b.dataset.id).map(i=>i.applicant.applicantId);openModal('ผู้สนใจหลักสูตร (ไม่ใช่ผลคัดเลือกแยกหลักสูตร)',selectionTable(selections.filter(s=>ids.includes(s.applicant.applicantId))));break;}
 case 'close':get('uiDialog').close();returnFocus?.focus();break;
 case 'delete-self':deleteDialog(true);break;case 'delete-teacher':get('uiDialog').close();deleteDialog(false);break;
 case 'delete-final':if(get('deleteConsent').checked&&b.dataset.ready==='true'){get('uiDialog').close();state.deletedTeacher=true;save('deletedTeacher');if(b.dataset.self==='true'){sessionStorage.removeItem('mfu.profile');auth('login');}else render('school-search');toast('ลบเฉพาะบัญชีตัวอย่างแล้ว');}break;
 case 'school-general':if(!teacherSchoolSaving){state.teacherSchoolTab='general';render('schools');}break;
 case 'school-director':if(!teacherSchoolSaving){state.teacherSchoolTab='director';render('schools');}break;case 'teacher-school-retry':void loadTeacherSchool();break;case 'teacher-school-cancel':if(!teacherSchoolSaving)render('schools');break;
 case 'unused-director':get('schoolEditor').innerHTML='<div class="notice">Entity School ที่ส่งมายังไม่มีข้อมูลผู้บริหาร วุฒิการศึกษา อีเมล/โทรศัพท์โรงเรียน หรือรูปโรงเรียน จึงรอ API ก่อนบันทึกส่วนนี้</div>';break;
 case 'teacher-applicants-retry':void loadTeacherApplicantSearch();break;case 'teacher-school-applicants':showOwnSchoolDetails();break;case 'search-tab':state.searchTab=b.dataset.tab;render('applicants');break;case 'own-school-stats':render('own-school-stats');break;case 'reset-school-lookup':get('ownSchoolFilter').reset();get('ownSchoolRows').innerHTML=ownSchoolRows();break;case 'report-list-page':state.reportListPage=+b.dataset.number;get('originalReportResults').innerHTML=originalReportResults();break;case 'report-tab':state.reportListPage=1;state.reportTab=b.dataset.tab;state.originalReportFilters={};state.teacherReportLoaded=false;state.teacherReportData=null;render('overview');break;case 'teacher-report-retry':void loadTeacherOverviewReport();break;case 'project-interest-detail':{const ids=new Set(reportInterestList().filter(i=>i.program.programId===+b.dataset.id).map(i=>i.applicant.applicantId));openModal('ผู้สนใจ '+esc(programs.find(p=>p.programId===+b.dataset.id)?.programName),selectionTable(scope(selections).filter(s=>ids.has(s.applicant.applicantId))));break;}case 'remove-photo':toast('ยังไม่เปิดใช้งานการลบรูปโปรไฟล์');break;case 'renew-start':void startRenewal();break;case 'renew-next':if(!renewalPending&&state.role==='teacher'&&state.renewal<3&&(state.renewal!==2||state.profile.isEmailVerified===true)){state.renewal++;render('renew');}break;case 'renew-back':if(!renewalPending&&state.renewal>1&&state.renewal<4){state.renewal--;render('renew');}break;
 case 'renew-confirm':void confirmRenewal(b);break;
 case 'export-applicant-page': {
    if (state.role !== 'teacher' || !get('applicantFilter')) break;

    const result = state.applicantSearchResult;

    if (!Array.isArray(result?.content) || !result.content.length) {
        toast('ไม่มีรายชื่อในหน้านี้สำหรับส่งออก');
        break;
    }

    download(
        `applicants-page-${result.currentPage + 1}.csv`,
        ['รหัสผู้สมัคร', 'ชื่อ-นามสกุล', 'โรงเรียน', 'สถานะล่าสุด'],
        result.content.map(applicant => [
            applicant.applicantId,
            applicant.studentName,
            applicant.schoolName,
            applicant.statusDescription || applicant.status || '—'
        ])
    );

    break;
}
 case 'export-applicants':download('applicants-demo.csv',['applicantId','firstName','lastName','academicYear','statusCode'],(state.page==='applicants'?filteredSelections():cohort(state.reportFilters||{})).map(s=>[s.applicant.applicantId,s.applicant.firstName,s.applicant.lastName,s.applicant.academicYear,s.status.statusCode]));break;
 case 'export-interests':download('program-interest-demo.csv',['programId','programName','interestedApplicants'],programs.map(p=>[p.programId,p.programName,(state.role==='admin'?adminInterests():scope(interests)).filter(i=>i.program.programId===p.programId).length]));break;
 case 'print':if(state.role==='teacher'&&state.page==='overview'){printTeacherOverviewReport();}else{window.print();}break;
 case 'month-prev':month.setMonth(month.getMonth()-1);calendar();break;case 'month-next':month.setMonth(month.getMonth()+1);calendar();break;case 'month-today':month=new Date();month.setDate(1);calendar();break;
 case 'admin-save-draft':{const f=get('postForm');f.elements.uiDraft.value='true';f.elements.visibleStartDate.required=false;f.elements.visibleEndDate.required=false;f.requestSubmit();break;}case 'admin-hide-post': {
    if (state.role !== 'admin' || b.disabled) break;

    const post = state.posts.find(
        item => item.postId === Number(b.dataset.id)
    );

    if (!post) {
        toast('ไม่พบข่าวที่ต้องการ');
        break;
    }

    b.disabled = true;

    void (async () => {
        try {
            const saved = await window.MfuApi.request(
                post.isHidden
                    ? 'PostController.show'
                    : 'PostController.hide',
                { params: { id: post.postId } }
            );

            if (saved?.postId !== post.postId ||
                typeof saved.isHidden !== 'boolean') {
                throw new Error('ข้อมูลสถานะข่าวจาก Backend ไม่ถูกต้อง');
            }

            state.posts = state.posts.map(item =>
                item.postId === saved.postId
                    ? { ...saved, uiDraft: saved.publishedAt == null }
                    : item
            );

            if (state.role === 'admin' &&
                state.page === 'project-detail' &&
                state.selectedPost === saved.postId) {
                adminPostDetail(saved.postId);
            }

            toast(saved.isHidden
                ? 'ซ่อนข่าวในฐานข้อมูลแล้ว'
                : 'แสดงข่าวในฐานข้อมูลแล้ว'
            );
        } catch (error) {
            toast(error.message || 'เปลี่ยนสถานะข่าวไม่สำเร็จ');
        } finally {
            b.disabled = false;
        }
    })();

    break;
}case 'admin-applicant-page':state.adminApplicantPage=+b.dataset.number;get('adminApplicantRows').innerHTML=adminApplicantTable();break;case 'admin-post-page':state.adminPostPage=+b.dataset.number;get('postRows').innerHTML=adminPostRows(state.adminFilteredPosts);break;case 'list-page':{const page=Number(b.dataset.number);if(state.role==='teacher'&&Number.isInteger(page)&&page>=0){state.teacherApplicantPage=page;updateTeacherApplicantPage();if(get('selectionRows'))get('selectionRows').innerHTML=pagedSelections();}break;}case 'calendar-day':showEvent(b.dataset.date);break;case 'chart-detail':chartDetail(b.dataset);break;
 }});
 document.addEventListener('submit',async e=>{const form=e.target;if(!['authForm','profileForm','postForm','schoolForm','otpForm','renewForm','applicantFilter','postFilter','adminPostFilter','schoolFilter','reportFilter','calendarFilter','directorForm','originalReportFilter','ownSchoolFilter','adminReportFilter','adminDashboardFilter','adminResultFilter','adminApplicantFilter','adminStudentFilter'].includes(form.id))return;e.preventDefault();const d=Object.fromEntries(new FormData(form));switch(form.id){
 case 'adminDashboardFilter':{const year=d.academicYear.trim();const days=Number(d.days);if((year&&!/^[0-9]{4}$/.test(year))||![7,30,90,365].includes(days)){toast('กรุณาตรวจสอบปีการศึกษาและช่วงเวลา');break;}state.dashboardFilters={academicYear:year,days};void loadAdminDashboard();break;}case 'adminResultFilter':if(state.adminResultsView==='applicants'){state.adminResultFilters=d;}else{state.adminResultGroupFilters=d;}state.adminResultsPage=0;render('results');break;case 'adminApplicantFilter':state.adminApplicantPage=1;state.adminFilters=d;get('adminApplicantRows').innerHTML=adminApplicantRows(d);break;case 'adminStudentFilter':get('adminStudentRows').innerHTML=selectionTable(selections.filter(s=>(!d.query||(s.applicant.firstName+' '+s.applicant.lastName+' '+s.applicant.applicantId).includes(d.query))&&(d.school==='ทุกโรงเรียน'||s.applicant.school.name===d.school)));break;case 'adminReportFilter':state.liveAdminReportFilters={...state.liveAdminReportFilters,[state.page]:{round:d.round||'',region:d.region||'',period:d.period||''}};render(state.page);break;case 'originalReportFilter':state.reportListPage=1;state.originalReportFilters=d;void loadTeacherOverviewReport();break;case 'ownSchoolFilter':get('ownSchoolRows').innerHTML=ownSchoolRows(d);break;case 'directorForm':await saveSchoolPrincipal(form,d);break;
 case 'calendarFilter':state.calendarFilters=d;calendar();break;
case 'authForm':
    if (state.auth === 'login') {
        await backendLogin(form, d);
    } else if (state.auth === 'reset') {
        const email = d.email.trim().toLowerCase();
        await window.MfuApi.request('LoginController.forgotPassword', {
            auth: false,
            body: { email }
        });
        toast('หากอีเมลนี้มีบัญชีในระบบ ระบบจะส่งลิงก์ตั้งรหัสผ่านให้');
        auth('login', authRole);
    } else if (state.auth === 'password' && state.passwordResetToken) {
        if (d.password !== d.confirm) {
            form.elements.confirm.setCustomValidity('รหัสผ่านไม่ตรงกัน');
            form.elements.confirm.reportValidity();
            return;
        }
        await window.MfuApi.request('LoginController.resetPassword', {
            auth: false,
            body: {
                token: state.passwordResetToken,
                newPassword: d.password
            }
        });
        state.passwordResetToken = null;
        toast('เปลี่ยนรหัสผ่านสำเร็จ กรุณาเข้าสู่ระบบ');
        auth('login', 'teacher');
    } else if (state.auth === 'register') {
        prepareRegistration(d);
    } else if (state.auth === 'password') {
        await submitRegistration(form, d);
    } else {
        auth('sent');
    }
    break; case 'profileForm': {
    if (profilePictureUpload) {
        toast('กำลังอัปโหลดรูปภาพ กรุณารอสักครู่');
        break;
    }
    if (state.role !== 'teacher' ||
        form.dataset.saving === 'true') {
        break;
    }

    const prefixId = Number(d.prefixId);
    const prefix = (state.profilePrefixes || [])
        .find(item => item.prefixId === prefixId);

    const firstName = (d.firstName || '').trim();
    const lastName = (d.lastName || '').trim();
    const phone = (d.phone || '').trim();

    if (!prefix || !firstName || !lastName) {
        toast('กรุณาเลือกคำนำหน้าและกรอกชื่อ-นามสกุล');
        break;
    }

    if (phone && !/^0[0-9]{9}$/.test(phone)) {
        toast('กรุณากรอกเบอร์โทรศัพท์ 10 หลัก เริ่มต้นด้วย 0');
        break;
    }

    const teacherId = state.profile.teacherId;
    const button = form.querySelector('button.btn-primary');
    const originalLabel = button?.textContent;

    form.dataset.saving = 'true';

    if (button) {
        button.disabled = true;
        button.textContent = 'กำลังบันทึก…';
    }

    try {
        const saved = await window.MfuApi.request(
            'GuidanceTeacherController.updateMyProfile',
            {
                body: {
                    prefix: { prefixId },
                    firstName,
                    lastName,
                    phone: phone || null
                }
            }
        );

        if (state.role !== 'teacher' ||
            state.profile.teacherId !== teacherId ||
            get('profileForm') !== form) {
            break;
        }

        if (saved?.teacherId !== teacherId ||
            !saved.prefix?.prefixId ||
            !saved.school?.schoolId) {
            throw new Error('ข้อมูลโปรไฟล์ที่ Backend ส่งกลับมาไม่ครบ');
        }

        state.profilePrefixId = saved.prefix.prefixId;
        state.profile = {
            ...state.profile,
            ...saved,
            schoolId: saved.school.schoolId,
            prefix: saved.prefix.prefixName
        };

        render('profile');
        toast('บันทึกข้อมูลส่วนตัวลงฐานข้อมูลแล้ว');
    } catch (error) {
        if (get('profileForm') === form) {
            toast(error.message || 'บันทึกข้อมูลส่วนตัวไม่สำเร็จ');
        }
    } finally {
        delete form.dataset.saving;

        if (button?.isConnected) {
            button.disabled = false;
            button.textContent = originalLabel;
        }
    }

    break;
}
case 'postForm': {
    if (state.role !== 'admin') {
        toast('เฉพาะผู้ดูแลระบบเท่านั้นที่บันทึกข่าวได้');
        break;
    }

    if (form.dataset.saving === 'true') break;

    const saveDraft =
        d.uiDraft === 'true' && !e.submitter?.dataset.publish;

    const id = d.postId ? Number(d.postId) : null;
    const existing = id
        ? state.posts.find(post => post.postId === id)
        : null;

    if (id && !existing) {
        toast('ไม่พบข่าวที่ต้องการแก้ไข กรุณาโหลดรายการใหม่');
        break;
    }

    if (saveDraft && existing?.publishedAt != null) {
        toast('ข่าวนี้เผยแพร่แล้ว ยังไม่ได้เชื่อมต่อการเปลี่ยนกลับเป็นฉบับร่าง');
        break;
    }

    if (d.isHidden === 'ซ่อน') {
        toast('ยังไม่ได้เชื่อมต่อการซ่อนข่าว กรุณาเลือกแสดงบนเว็บไซต์สำหรับขั้นตอนนี้');
        break;
    }

    if ([d.venue, d.subdistrict, d.district, d.province, d.zipcode]
        .some(value => String(value || '').trim())) {
        toast('ยังไม่ได้เชื่อมต่อข้อมูลสถานที่ กรุณาเว้นช่องสถานที่ไว้สำหรับการทดสอบนี้');
        break;
    }

    const body = {
        title: (d.title || '').trim(),
        summary: (d.summary || '').trim(),
        content: (d.content || '').trim(),
        targetAudience: (d.targetAudience || '').trim(),
        postType: (d.postType || '').trim().toUpperCase(),
        eventType: (d.eventType || '').trim() || null,
        eventStartDate: d.eventStartDate || null,
        eventEndDate: d.eventEndDate || null,
        visibleStartDate: d.visibleStartDate || null,
        visibleEndDate: d.visibleEndDate || null,
        academicYear: existing?.academicYear ?? null,
        applicationRound: existing?.applicationRound ?? null
    };

    if (!saveDraft && (
        !body.title ||
        !body.summary ||
        !body.content ||
        !body.targetAudience ||
        !body.visibleStartDate ||
        !body.visibleEndDate
    )) {
        toast('กรุณากรอกชื่อเรื่อง สรุป เนื้อหา กลุ่มเป้าหมาย และช่วงแสดงผลให้ครบ');
        break;
    }

    if (
        (body.eventStartDate && body.eventEndDate &&
            body.eventEndDate < body.eventStartDate) ||
        (body.visibleStartDate && body.visibleEndDate &&
            body.visibleEndDate < body.visibleStartDate)
    ) {
        toast('วันสิ้นสุดต้องไม่อยู่ก่อนวันเริ่มต้น');
        break;
    }

    const buttons = [...form.querySelectorAll('button')];
    const previousDisabled = buttons.map(button => button.disabled);
    let savedToDatabase = false;

    const rememberPost = post => {
        state.posts = [
            { ...post, uiDraft: post.publishedAt == null },
            ...state.posts.filter(item => item.postId !== post.postId)
        ];
    };

    form.dataset.saving = 'true';
    buttons.forEach(button => button.disabled = true);

    try {
        let saved = await window.MfuApi.request(
            id ? 'PostController.update' : 'PostController.create',
            {
                ...(id ? { params: { id } } : {}),
                body
            }
        );

        if (!saved?.postId) {
            throw new Error('Backend ไม่ส่งรหัสข่าวกลับมา');
        }

        savedToDatabase = true;
        form.elements.postId.value = saved.postId;
        rememberPost(saved);

        // Publish drafts only. Editing published content keeps its timestamp.
        if (!saveDraft && saved.publishedAt == null) {
            saved = await window.MfuApi.request(
                'PostController.publish',
                { params: { id: saved.postId } }
            );

            if (!saved?.postId || !saved.publishedAt) {
                throw new Error('Backend ไม่ยืนยันสถานะเผยแพร่');
            }

            rememberPost(saved);
        }

        render('posts');
        toast(saveDraft
            ? 'บันทึกฉบับร่างลงฐานข้อมูลแล้ว'
            : 'บันทึกข่าวที่เผยแพร่ลงฐานข้อมูลแล้ว'
        );
    } catch (error) {
        const message = error.message || 'ดำเนินการไม่สำเร็จ';

        toast(savedToDatabase
            ? 'บันทึกข้อมูลแล้ว แต่ขั้นตอนถัดไปไม่สำเร็จ: ' + message
            : message
        );
    } finally {
        delete form.dataset.saving;
        buttons.forEach((button, index) => {
            button.disabled = previousDisabled[index];
        });
    }

    break;
} case 'schoolForm':await saveTeacherSchool(form,d);break;
 case 'otpForm':toast('กรุณาใช้ขั้นตอนต่ออายุจากบัญชีที่ยืนยันอีเมลแล้ว');break;
 case 'renewForm':prepareRenewal(d);break;
case 'applicantFilter':state.teacherApplicantFilters={round:d.round||'',year:d.year||'',program:d.program||'',query:d.query||''};await loadTeacherApplicantSearch();break; case 'unused-applicantFilter':get('selectionRows').innerHTML=selectionTable(scope(selections).filter(s=>(s.applicant.firstName+' '+s.applicant.lastName+' '+s.applicant.applicantId).includes(d.query.trim())&&s.applicant.academicYear===+d.year&&(!d.round||d.round==='ทุกรอบ'||d.round===s.entryGroup.entrygroupName)&&(d.status==='ทั้งหมด'||d.status===s.status.description)));break;
case 'postFilter': {
    const query = (d.query || '').trim().toLowerCase();

    const posts = state.posts.filter(post =>
        !post.isHidden &&
        (!d.type || d.type === 'ทั้งหมด' || post.postType === d.type) &&
        (!query || [post.title, post.summary, post.content].some(value =>
            String(value || '').toLowerCase().includes(query)
        ))
    );

    get('filteredPosts').innerHTML = postCards(posts);
    break;
} case 'adminPostFilter':state.adminPostPage=1;get('postRows').innerHTML=adminPostRows(state.posts.filter(p=>p.title.includes(d.query)&&(d.visible==='ทั้งหมด'||adminPostStatus(p)===d.visible)&&(d.period!=='30 วันล่าสุด'||new Date(p.publishedAt)>=new Date(Date.now()-30*864e5))));break;case 'unused-adminPostFilter':get('postRows').innerHTML=postTable(state.posts.filter(p=>p.title.includes(d.query)&&(d.visible==='ทั้งหมด'||(d.visible==='ซ่อน')===p.isHidden)&&(state.page!=='expired'||p.visibleEndDate<new Date().toISOString().slice(0,10))));break;
 case 'schoolFilter':if(state.role==='admin'){state.adminSchoolFilters={keyword:d.query.trim(),province:d.province==='ทุกจังหวัด'?'':d.province};void loadAdminSchools(0);}break;
 case 'reportFilter':{state.reportFilters=d;get('reportCharts').innerHTML=managementAnalytics(d);return;}
 case 'unused-reportFilter':{const list=+d.year===2569?scope(interests).filter(i=>d.school==='ทุกโรงเรียน'||i.applicant.school.name===d.school):[];get('reportCharts').innerHTML=bars('ความสนใจหลักสูตร · '+esc(d.year),programs.map(p=>p.programName),programs.map(p=>list.filter(i=>i.program.programId===p.programId).length));break;}
 }});
 // Reports use one filtered cohort for every chart, rate and drill-down.
 function cohort(filters={}){return scope(selections).filter(s=>(!filters.year||+filters.year===s.applicant.academicYear)&&(!filters.round||filters.round==='ทุกรอบ'||filters.round===s.entryGroup.entrygroupName)&&(!filters.school||filters.school==='ทุกโรงเรียน'||filters.school===s.applicant.school.name));}
 function chart(title,labels,values,kind='bar',key='status'){
  const total=values.reduce((a,b)=>a+b,0),max=Math.max(1,...values),colors=['#7A2222','#C9A227','#D97706','#15803D','#636366','#7B241C'];let acc=0;
  const legend=labels.map((l,i)=>`<button class="chart-legend" data-ui="chart-detail" data-key="${key}" data-value="${esc(l)}"><i class="dot" style="background:${colors[i%colors.length]}"></i><span>${esc(l)}</span><b>${values[i]}</b><small>${total?(values[i]/total*100).toFixed(1):'0.0'}%</small></button>`).join('');
  return panel(`<div class="chart-heading"><h3>${title}</h3><small>${key==='stage'?'กลุ่มสถานะอาจซ้อนกัน':total.toLocaleString()+' รายการ'}</small></div>${total?kind==='pie'?`<div class="donut-layout"><div class="donut" role="img" aria-label="${esc(title)}" style="background:conic-gradient(${values.map((v,i)=>{const start=acc;acc+=v/total*100;return `${colors[i%colors.length]} ${start}% ${acc}%`;}).join(',')})"><strong>${total}<small>รายการ</small></strong></div><div class="legend">${legend}</div></div>`:`<div class="bars">${labels.map((l,i)=>`<button class="bar-row" data-ui="chart-detail" data-key="${key}" data-value="${esc(l)}"><span>${esc(l)}</span><div class="bar-track"><i style="width:${values[i]/max*100}%"></i></div><strong>${values[i]}</strong></button>`).join('')}</div>`:'<div class="empty-state"><strong>ไม่พบข้อมูลตามเงื่อนไข</strong><p>ลองเปลี่ยนปีการศึกษาหรือรอบสมัคร</p></div>'}`);
 }
 function stats(){return analytics({},true);}
 function analytics(filters={},compact=false){const list=cohort(filters),ids=new Set(list.map(s=>s.applicant.applicantId));const ints=scope(interests).filter(i=>ids.has(i.applicant.applicantId));const byStatus=statuses.map(t=>list.filter(s=>s.status.statusCode===t.statusCode).length);const paid=list.filter(s=>s.status.statusCode==='50').length, selected=list.filter(s=>['40','41','42','43','50'].includes(s.status.statusCode)).length;const interested=new Set(ints.map(i=>i.applicant.applicantId)).size;state.chartCohort=list;state.chartInterests=ints;
  const summary=kpis(['ผู้สมัครทั้งหมด','ได้รับการคัดเลือก¹','ชำระค่าลงทะเบียนแล้ว','อัตราชำระจากผู้ได้รับคัดเลือก'],[ids.size,selected,paid,selected?(paid/selected*100).toFixed(1)+'%':'—']);
  const charts=`<div class="chart-grid">${chart('จำนวนผู้สนใจแยกตามหลักสูตร',programs.map(p=>p.programName),programs.map(p=>ints.filter(i=>i.program.programId===p.programId).length),'bar','program')}${chart('สัดส่วนสถานะผู้สมัคร',statuses.map(s=>s.statusCode+' · '+s.description),byStatus,'pie','status')}</div>`;
  if(compact)return summary+charts;
  return summary+`<div class="report-summary"><strong>สรุปสำหรับผู้บริหาร</strong><p>ผู้สมัคร ${ids.size} คน · ผู้บันทึกความสนใจ ${interested} คน · ความสนใจ ${ints.length} รายการ (หนึ่งคนสนใจได้หลายหลักสูตร)</p><p>ได้รับการคัดเลือก¹ ${selected} คน ชำระค่าลงทะเบียนแล้ว ${paid} คน คิดเป็น ${selected?(paid/selected*100).toFixed(1):'0.0'}% ของผู้ที่ได้รับการคัดเลือก</p><small>¹ กลุ่มรหัส 40, 41, 42, 43 และ 50 รวมผู้สละสิทธิ์ เพื่อแสดงประวัติการได้รับคัดเลือก ไม่ใช่ยอดผู้คงสิทธิ์ปัจจุบัน · กดกราฟเพื่อดูรายชื่อที่ใช้คำนวณ</small></div>`+charts+`<div class="chart-grid">${chart('ผู้สมัครตามรอบรับสมัคร',entryGroups.map(g=>g.entrygroupName),entryGroups.map(g=>list.filter(s=>s.entryGroup.entrygroupId===g.entrygroupId).length),'bar','round')}${chart('ผลตอบรับการขึ้นทะเบียน',['ชำระแล้ว','ได้รับคัดเลือก ยังไม่ชำระ','ขั้นตอนก่อนคัดเลือก'],[paid,selected-paid,list.length-selected],'pie','registration')}${chart('สถานะผู้ได้รับคัดเลือก',['40 · ได้รับการคัดเลือก','41 · ไม่ยืนยันทะเบียน','42 · สละสิทธิ์','43 · ยกเลิกสละสิทธิ์','50 · ชำระค่าลงทะเบียนแล้ว'],['40','41','42','43','50'].map(c=>list.filter(s=>s.status.statusCode===c).length),'bar','status')}${chart('ความสนใจตามสำนักวิชา',[...new Set(programs.map(p=>p.faculty))],[...new Set(programs.map(p=>p.faculty))].map(f=>ints.filter(i=>i.program.faculty===f).length),'bar','faculty')}${state.role==='admin'?chart('ผู้สมัครแยกตามโรงเรียน',schools.map(s=>s.name),schools.map(s=>list.filter(a=>a.applicant.school.schoolId===s.schoolId).length),'bar','school'):''}</div><p class="report-source">ขอบเขต: ${state.role==='teacher'?esc(ownSchool().name):esc(filters.school||'ทุกโรงเรียน')} · ปีการศึกษา ${esc(filters.year||'2569')} · ${esc(filters.round||'ทุกรอบ')} · ข้อมูลที่ใช้คำนวณเป็นชุดสาธิต จนกว่าจะเชื่อม API</p>`;
 }
 function reportPage(page){state.reportFilters={};return heading(page==='analysis'?'วิเคราะห์ความสนใจหลักสูตร':page==='reports'?'รายงานภาพรวมระดับระบบ':'รายงานภาพรวมผู้สมัคร','สรุปข้อมูลการสมัครและผลตอบรับ เพื่อประกอบการตัดสินใจ')+panel(`<form id="reportFilter" class="filter-panel">${select('ปีการศึกษา','year',['2569','2568'])}${select('รอบรับสมัคร','round',['ทุกรอบ',...entryGroups.map(g=>g.entrygroupName)])}${state.role==='admin'?select('โรงเรียน','school',['ทุกโรงเรียน',...schools.map(s=>s.name)]):''}<button class="btn btn-primary">แสดงรายงาน</button>${btn('ส่งออกข้อมูล','export-applicants')}${btn('พิมพ์รายงาน / PDF','print')}</form>`)+`<div id="reportCharts">${managementAnalytics()}</div>`;}
 function chartDetail(d){let list=state.chartCohort||scope(selections);const v=d.value;if(d.key==='status')list=list.filter(s=>s.status.statusCode===v.split(' · ')[0]);else if(d.key==='round')list=list.filter(s=>s.entryGroup.entrygroupName===v);else if(d.key==='school')list=list.filter(s=>s.applicant.school.name===v);else if(d.key==='program'||d.key==='faculty'){const ids=new Set((state.chartInterests||scope(interests)).filter(i=>d.key==='program'?i.program.programName===v:i.program.faculty===v).map(i=>i.applicant.applicantId));list=list.filter(s=>ids.has(s.applicant.applicantId));}else if(d.key==='stage')list=list.filter(s=>v==='สมัครเข้าศึกษา'?true:v==='มีสิทธิ์สอบ/สัมภาษณ์'?['20','21','22','23','24','25','26','27','28'].includes(s.status.statusCode):v==='ได้รับการคัดเลือก'?['40','41','42','43','50'].includes(s.status.statusCode):s.status.statusCode==='50');else if(d.key==='week')list=list.filter(s=>'สัปดาห์ที่ '+(Math.min(3,Math.floor((new Date(s.appliedAt).getDate()-1)/7))+1)===v);else if(d.key==='year')list=list.filter(s=>String(s.applicant.academicYear)===v);else if(d.key==='outcome')list=list.filter(s=>v==='ชำระแล้ว'?s.status.statusCode==='50':v==='สละสิทธิ์'?s.status.statusCode==='42':['40','41','43'].includes(s.status.statusCode));else if(d.key==='registration')list=list.filter(s=>v==='ชำระแล้ว'?s.status.statusCode==='50':v==='ได้รับคัดเลือก ยังไม่ชำระ'?['40','41','42','43'].includes(s.status.statusCode):!['40','41','42','43','50'].includes(s.status.statusCode));openModal(esc(v),`<p>จำนวน ${list.length} คน</p>${selectionTable(list)}`);}
 function postDetail(id){if(state.role==='admin'){adminPostDetail(id);return;}const p=state.posts.find(x=>x.postId===+id);if(!p)return;state.page='project-detail';state.selectedPost=p.postId;get('extendedPage').innerHTML=(state.role==='teacher'?`<div class="school-context">${esc(ownSchool().name)}</div>`:'')+heading('รายละเอียดข่าว')+`<div class="detail-layout">${panel(`<div class="card-title-row">${tag(p.postType)}${pinButton(p.postId)}</div><h1>${esc(p.title)}</h1><div class="detail-date">ช่วงประชาสัมพันธ์ ${esc(p.visibleStartDate)} – ${esc(p.visibleEndDate)}</div><div class="detail-copy preserved">${esc(p.content)}</div>`)}${panel(`<h3>สอบถามข้อมูลโครงการ</h3><p>ส่วนรับนักศึกษา</p><p>โทร. 0 5391 6103</p>${state.role==='admin'?btn('แก้ไขข่าว','post-edit','btn-primary',`data-id="${p.postId}"`):''}<p>${btn('กลับหน้าข่าว','nav','btn-outline',`data-target="${state.role==='admin'?'posts':'dashboard'}"`)}</p>`)}</div>`;}
 function calendarPage(){return heading('ปฏิทินกำหนดการรับสมัคร','ติดตามกำหนดการ TCAS')+panel(`<form id="calendarFilter" class="filter-panel"><label>
    ปีการศึกษา
    <select name="year">
        <option value="">ทุกปีการศึกษา</option>
        ${[...new Set(
            (state.calendarApiEvents || [])
                .map(event => event.academicYear)
                .filter(year => year != null)
        )].sort((a, b) => b - a).map(year => `
            <option value="${esc(year)}">${esc(year)}</option>
        `).join('')}
    </select>
</label>
<label>
    รอบการสมัคร
    <select name="round">
        <option value="">ทุกรอบ</option>
        ${[...new Set(
            (state.calendarApiEvents || [])
                .map(event => event.round)
                .filter(round => round && round.trim())
        )].sort().map(round => `
            <option value="${esc(round)}">${esc(round)}</option>
        `).join('')}
    </select>
</label>${field('ค้นหาชื่อโครงการ','query')}<button type="reset" class="btn btn-outline">ล้างตัวกรอง</button><button class="btn btn-primary">ค้นหา</button></form>`)+`<div class="calendar-layout">${panel(`<div class="calendar-toolbar"><h2 id="newMonth"></h2><div>${btn('‹','month-prev')}${btn('วันนี้','month-today')}${btn('›','month-next')}</div></div><div class="calendar-grid" id="newCalendar"></div><div class="calendar-legend">● เปิดรับสมัคร　 ● ปิดรับสมัคร　 ● ประกาศผล　 ● ยืนยันสิทธิ์</div><p class="calendar-foot">เลือกกำหนดการในปฏิทินหรือรายการด้านขวาเพื่อดูรายละเอียด</p>`)}${panel('<h2>กำหนดการ</h2><div id="upcomingEvents"></div><h2 class="event-divider">รายละเอียดกำหนดการ</h2><div id="eventDetail" class="event-empty">เลือกกำหนดการเพื่อดูรายละเอียด</div>')}</div>`;}
function calendarEvents() {
    const filters = state.calendarFilters || {};
    const query = (filters.query || '').trim().toLowerCase();

    const eventLabels = {
        APPLICATION_OPEN: 'เปิดรับสมัคร',
        APPLICATION_CLOSE: 'ปิดรับสมัคร',
        RESULT_ANNOUNCEMENT: 'ประกาศผล',
        RIGHTS_CONFIRMATION: 'ยืนยันสิทธิ์'
    };

    const eventColors = {
        APPLICATION_OPEN: 0,
        APPLICATION_CLOSE: 1,
        RESULT_ANNOUNCEMENT: 2,
        RIGHTS_CONFIRMATION: 3
    };

    return (state.calendarApiEvents || [])
        .filter(event =>
            (!filters.year ||
                Number(filters.year) === event.academicYear) &&
            (!filters.round ||
                filters.round === 'ทุกรอบ' ||
                filters.round === event.round) &&
            (!query ||
                String(event.eventTitle || '').toLowerCase().includes(query) ||
                String(event.round || '').toLowerCase().includes(query))
        )
        .map(event => ({
            id: event.eventId,
            date: event.eventStartDate || event.eventDate,
            endDate: event.eventEndDate ||
                event.eventStartDate || event.eventDate,
            title: event.sourcePostId != null
                ? event.eventTitle
                : eventLabels[event.eventType] || event.eventTitle,
            round: event.round || '',
            color: eventColors[event.eventType] ?? 0,
            academicYear: event.academicYear,
            sourcePostId: event.sourcePostId
        }))
        .filter(event => event.date && event.endDate);
}
function calendar() {
    if (!get('newCalendar')) return;

    const events = calendarEvents();
    const year = month.getFullYear();
    const monthIndex = month.getMonth();

    const dateKey = date =>
        `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

    const formatDate = value =>
        new Date(value + 'T00:00:00').toLocaleDateString('th-TH', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });

    const firstDate = dateKey(new Date(year, monthIndex, 1));
    const lastDate = dateKey(new Date(year, monthIndex + 1, 0));
    const today = dateKey(new Date());

    get('newMonth').textContent = month.toLocaleDateString('th-TH', {
        month: 'long',
        year: 'numeric'
    });

    let html = ['อา.', 'จ.', 'อ.', 'พ.', 'พฤ.', 'ศ.', 'ส.']
        .map(day => `<div class="calendar-cell day-name">${day}</div>`)
        .join('');

    const firstWeekday = new Date(year, monthIndex, 1).getDay();

    for (let index = 0; index < firstWeekday; index++) {
        html += '<div class="calendar-cell blank"></div>';
    }

    const days = new Date(year, monthIndex + 1, 0).getDate();

    for (let day = 1; day <= days; day++) {
        const date = dateKey(new Date(year, monthIndex, day));
        const dailyEvents = events.filter(event =>
            event.date <= date && event.endDate >= date
        );

        html += `
            <button
                class="calendar-cell ${date === today ? 'today' : ''}"
                data-ui="calendar-day"
                data-date="${date}"
            >
                ${day}
                ${dailyEvents.map(event => `
                    <span class="calendar-event event-${event.color}">
                        ${esc(event.title)}
                    </span>
                `).join('')}
            </button>
        `;
    }

    get('newCalendar').innerHTML = html;

    const visible = events
        .filter(event =>
            event.date <= lastDate && event.endDate >= firstDate
        )
        .sort((a, b) => a.date.localeCompare(b.date));

    get('upcomingEvents').innerHTML = visible.length
        ? visible.map(event => `
            <button
                class="upcoming-item"
                data-ui="calendar-day"
                data-date="${event.date < firstDate ? firstDate : event.date}"
            >
                <span class="event-bullet event-${event.color}">●</span>
                <span>
                    ${esc(event.title)}
                    <small>
                        ${esc(event.round)}
                        <br>
                        ${formatDate(event.date)}
                        ${event.endDate !== event.date
                            ? ' – ' + formatDate(event.endDate)
                            : ''}
                    </small>
                </span>
            </button>
        `).join('')
        : '<p class="muted">ไม่มีกำหนดการในเดือนนี้</p>';

    get('eventDetail').textContent = 'เลือกกำหนดการเพื่อดูรายละเอียด';
}

function showEvent(date) {
    const list = calendarEvents().filter(event =>
        event.date <= date && event.endDate >= date
    );

    const formatDate = value =>
        new Date(value + 'T00:00:00').toLocaleDateString('th-TH', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });

    get('eventDetail').innerHTML = list.length
        ? list.map(event => `
            <h3>${esc(event.title)}</h3>
            <p>${esc(event.round)}</p>
            <p>
                ${formatDate(event.date)}
                ${event.endDate !== event.date
                    ? ' – ' + formatDate(event.endDate)
                    : ''}
            </p>
        `).join('')
        : 'ไม่มีกำหนดการในวันที่เลือก';
}
  // Figma-aligned applicant search: overview, two charts, filters and paginated list.
    function applicantRoundChart() {
    const statistics = state.applicantRoundStatistics;

    if (!statistics || !Array.isArray(statistics.rounds)) {
        return panel(`
            <h3>สัดส่วนรอบสมัคร TCAS</h3>
            <div class="empty-state">
                <strong>ยังไม่พร้อมแสดงข้อมูล</strong>
                <p>ไม่สามารถโหลดสถิติรอบสมัครได้</p>
            </div>
        `);
    }

    const rounds = statistics.rounds;
    const total = rounds.reduce((sum, item) => sum + item.count, 0);
    const colors = [
        '#7A2222', '#C9A227', '#D97706', '#15803D', '#636366'
    ];

    let position = 0;

    const gradient = rounds.map((item, index) => {
        const start = position;
        position += total ? item.count / total * 100 : 0;

        return `${colors[index % colors.length]} ${start}% ${position}%`;
    }).join(', ');

    return panel(`
        <h3>สัดส่วนรอบสมัคร TCAS</h3>
        <p class="muted">
            ปีการศึกษา ${esc(statistics.academicYear)}
            · รอบจากผลคัดเลือกล่าสุดที่เผยแพร่
        </p>

        ${total ? `
            <div class="donut-layout">
                <div
                    class="donut"
                    role="img"
                    aria-label="${esc(
                        rounds.map(item =>
                            `${item.label}: ${item.count} คน`
                        ).join(', ')
                    )}"
                    style="background:conic-gradient(${gradient})"
                >
                    <strong>${total}<small>คน</small></strong>
                </div>

                <div>
                    ${rounds.map((item, index) => `
                        <p>
                            <i
                                class="dot"
                                style="background:${colors[index % colors.length]}"
                            ></i>
                            ${esc(item.label)}
                            <b>${item.count}</b>
                            <small>
                                (${(item.count / total * 100).toFixed(1)}%)
                            </small>
                        </p>
                    `).join('')}
                </div>
            </div>
        ` : `
            <div class="empty-state">
                <strong>ไม่มีข้อมูลผู้สมัครในปีการศึกษานี้</strong>
            </div>
        `}
    `);
}

function applicantProgramChart() {
    const statistics = state.applicantProgramStatistics;

    if (!statistics || !Array.isArray(statistics.programs)) {
        return panel(`
            <h3>หลักสูตรที่สนใจ 5 อันดับแรก</h3>
            <div class="empty-state">
                <strong>ยังไม่พร้อมแสดงข้อมูล</strong>
                <p>ไม่สามารถโหลดสถิติความสนใจหลักสูตรได้</p>
            </div>
        `);
    }

    const topPrograms = statistics.programs.slice(0, 5);
    const maximum = Math.max(
        1,
        ...topPrograms.map(program => program.interestCount)
    );

    return panel(`
        <h3>หลักสูตรที่สนใจ 5 อันดับแรก</h3>
        <p class="muted">
            ปีการศึกษา ${esc(statistics.academicYear)}
            · ผู้สนใจ ${statistics.numberOfApplicants} คน
            · ความสนใจทั้งหมด ${statistics.totalInterests} รายการ
        </p>

        ${topPrograms.length ? `
            <div class="bars">
                ${topPrograms.map(program => `
                    <div class="bar-row">
                        <span>${esc(program.programName)}</span>
                        <div class="bar-track" aria-hidden="true">
                            <i style="width:${
                                program.interestCount / maximum * 100
                            }%"></i>
                        </div>
                        <strong>${program.interestCount}</strong>
                    </div>
                `).join('')}
            </div>
            <p class="muted">
                จำนวนรายการความสนใจ · หนึ่งคนสนใจได้หลายหลักสูตร
                <br>
                เมื่อจำนวนเท่ากัน เรียงตามรหัสหลักสูตร
            </p>
        ` : `
            <div class="empty-state">
                <strong>ไม่มีข้อมูลความสนใจหลักสูตรในปีการศึกษานี้</strong>
            </div>
        `}
    `);
}

  function applicantsPage() {
        const overview = state.applicantOverview;

    const metrics = [
        ['ผู้สมัคร', 'totalApplicants'],
        ['มีสิทธิ์สอบ', 'eligibleForExamination'],
        ['ได้รับการคัดเลือก', 'passedSelection'],
        ['ยืนยันสิทธิ์', 'rightsConfirmed'],
        ['สละสิทธิ์', 'rightsWaived'],
        ['ชำระค่าลงทะเบียน', 'enrolled']
    ];

    const statistics = `
        <div class="kpi-grid">
            ${metrics.map(([label, key]) => {
                const value = overview?.[key];
                const available = Number.isInteger(value) && value >= 0;

                return `
                    <div class="panel kpi">
                        <span>${esc(label)}</span>
                        <strong>${available
                            ? value.toLocaleString('th-TH')
                            : '—'}</strong>
                        <small>${available
                            ? 'ข้อมูลทั้งโรงเรียน · ทุกปีการศึกษา'
                            : 'ยังไม่พร้อมแสดงสถิติ'}</small>
                    </div>
                `;
            }).join('')}
        </div>
    `;

    const chartPlaceholder = title => panel(`
        <h3>${esc(title)}</h3>
        <div class="empty-state">
            <strong>ยังไม่พร้อมแสดงข้อมูล</strong>
            <p>จะแสดงเมื่อเชื่อมต่อข้อมูลสถิติแล้ว</p>
        </div>
    `);

    return heading('ค้นหาข้อมูลผู้สมัคร') + `
        ${lookupTabs('applicants')}

        <div class="school-notice">
            ข้อมูลที่แสดงเฉพาะ ${esc(ownSchool().name)}
        </div>

        <div class="section-caption">
            ข้อมูลภาพรวมโรงเรียน
            <small>สถิติจากผลที่เผยแพร่แล้ว · ไม่เปลี่ยนตามการค้นหารายชื่อ</small>
        </div>

        ${statistics}

        <div class="search-charts">
            ${applicantRoundChart()}
            ${applicantProgramChart()}
        </div>

        ${teacherApplicantFiltersMarkup()}


        ${panel(`
            <div class="section-caption">
                <strong>รายชื่อผู้สมัคร</strong>
                <button
    class="btn btn-outline"
    type="button"
    data-ui="export-applicant-page"
    title="ส่งออกเฉพาะรายชื่อในหน้าที่แสดง"
>
    ส่งออกหน้านี้ (CSV)
</button>
            </div>
            <div id="selectionRows">${pagedSelections()}</div>
        `)}
    `;
}


 function filteredSelections(){const d=state.applicantFilters||{};return scope(selections).filter(s=>(!d.year||s.applicant.academicYear===+d.year)&&(!d.query||(s.applicant.firstName+' '+s.applicant.lastName+' '+s.applicant.applicantId).includes(d.query.trim()))&&(!d.status||d.status==='ทั้งหมด'||d.status===s.status.description)&&(!d.round||d.round==='ทุกรอบ'||d.round===s.entryGroup.entrygroupName));}
function pagedSelections() {
    if(state.teacherApplicantLoading)return '<p role="status">กำลังโหลดข้อมูลผู้สมัคร…</p>';
    if(state.teacherApplicantError)return `<p role="alert">${esc(state.teacherApplicantError)}</p>${btn('ลองอีกครั้ง','teacher-applicants-retry','btn-primary')}`;
    const result = state.applicantSearchResult;

    if (!result || !Array.isArray(result.content)) {
        return `
            <div class="empty-state" role="status">
                <strong>ยังไม่ได้โหลดข้อมูลผู้สมัคร</strong>
            </div>
        `;
    }

    const rows = result.content.map(applicant => [
        esc(applicant.applicantId),
        esc(applicant.studentName),
        esc(applicant.schoolName),
        tag(applicant.statusDescription || applicant.status || '—')
    ]);

    const first = result.content.length
        ? result.currentPage * result.pageSize + 1
        : 0;

    const last = result.content.length
        ? first + result.content.length - 1
        : 0;

    const hasPrevious = result.currentPage > 0;
    const hasNext = result.currentPage + 1 < result.totalPages;

    return table(
        ['รหัสผู้สมัคร', 'ชื่อ-นามสกุล', 'โรงเรียน', 'สถานะล่าสุด'],
        rows
    ) + `
        <div class="pagination">
            <span>
                แสดง ${first}–${last} จาก ${result.totalItems} รายการ
            </span>
            <div>
                <button
                    type="button"
                    class="btn btn-outline"
                    data-ui="list-page"
                    data-number="${result.currentPage - 1}"
                    ${hasPrevious ? '' : 'disabled'}
                >
                    ก่อนหน้า
                </button>
                <span>
                    หน้า ${result.totalPages ? result.currentPage + 1 : 0}
                    / ${result.totalPages}
                </span>
                <button
                    type="button"
                    class="btn btn-outline"
                    data-ui="list-page"
                    data-number="${result.currentPage + 1}"
                    ${hasNext ? '' : 'disabled'}
                >
                    ถัดไป
                </button>
            </div>
        </div>
    `;
} function groupedChart(title,groups,series,values,key='faculty'){const max=Math.max(1,...values.flat());const colors=['#7A2222','#C9A227','#8E8E8E'];return panel(`<h3>${title}</h3><div class="group-legend">${series.map((l,i)=>`<span><i class="dot" style="background:${colors[i]}"></i>${esc(l)}</span>`).join('')}</div><div class="vertical-chart">${groups.map((g,i)=>`<button type="button" class="vertical-group" data-ui="chart-detail" data-key="${key}" data-value="${esc(g)}"><div class="vertical-bars">${values[i].map((v,j)=>`<div style="height:${v/max*175}px;background:${colors[j]}"><span>${v}</span></div>`).join('')}</div><span class="axis-label">${esc(g)}${!values[i].length?'<small>ไม่มีข้อมูล</small>':''}</span></button>`).join('')}</div>`);}
 function managementAnalytics(filters={}){const list=cohort(filters),selected=s=>['40','41','42','43','50'].includes(s.status.statusCode),paid=list.filter(s=>s.status.statusCode==='50'),chosen=list.filter(selected),ids=new Set(list.map(s=>s.applicant.applicantId));const ints=scope(interests).filter(i=>ids.has(i.applicant.applicantId));state.chartCohort=list;state.chartInterests=ints;const faculties=[...new Set(programs.map(p=>p.faculty))];const facultyIds=f=>new Set(ints.filter(i=>i.program.faculty===f).map(i=>i.applicant.applicantId));const weekly=Array.from({length:4},(_,i)=>list.filter(s=>Math.min(3,Math.floor((new Date(s.appliedAt).getDate()-1)/7))===i).length);const n=c=>list.filter(s=>c.includes(s.status.statusCode)).length;
 return kpis(['ผู้สมัครทั้งหมด','มีสิทธิ์สอบ/สัมภาษณ์','ได้รับการคัดเลือก¹','ชำระค่าลงทะเบียนแล้ว','สละสิทธิ์','ไม่ยืนยันทะเบียน'],[ids.size,n(['20','21','22','23','24','25','26','27','28']),chosen.length,paid.length,n(['42']),n(['41'])])+`<div class="chart-grid report-four">${chart('ขั้นตอนการสมัคร (สถานะปัจจุบัน)',['สมัครเข้าศึกษา','มีสิทธิ์สอบ/สัมภาษณ์','ได้รับการคัดเลือก','ชำระค่าลงทะเบียนแล้ว'],[list.length,n(['20','21','22','23','24','25','26','27','28']),chosen.length,paid.length],'bar','stage')}${chart('ผลหลังการคัดเลือก',['ชำระแล้ว','สละสิทธิ์','ได้รับคัดเลือก ยังไม่ชำระ'],[paid.length,n(['42']),chosen.length-paid.length-n(['42'])],'pie','outcome')}${groupedChart('ผู้สนใจและผู้ได้รับการคัดเลือกตามสำนักวิชา',faculties,['ผู้สนใจ','ได้รับการคัดเลือก'],faculties.map(f=>{const set=facultyIds(f);return [set.size,list.filter(s=>set.has(s.applicant.applicantId)&&selected(s)).length]}))}${chart('สัดส่วนตามรอบรับสมัคร',entryGroups.map(g=>g.entrygroupName),entryGroups.map(g=>list.filter(s=>s.entryGroup.entrygroupId===g.entrygroupId).length),'pie','round')}${chart('จำนวนการสมัครรายสัปดาห์',['สัปดาห์ที่ 1','สัปดาห์ที่ 2','สัปดาห์ที่ 3','สัปดาห์ที่ 4'],weekly,'bar','week')}${groupedChart('เปรียบเทียบรายปีการศึกษา',['2567','2568','2569'],['ผู้สมัคร','ได้รับการคัดเลือก','ชำระแล้ว'],[[],[],[list.length,chosen.length,paid.length]],'year')}</div>${chart('รายละเอียดสถานะผู้สมัครตามรหัส',statuses.map(s=>s.statusCode+' · '+s.description),statuses.map(t=>n([t.statusCode])),'bar','status')}<div class="report-summary"><strong>สรุปข้อมูลประกอบการตัดสินใจ</strong><p>จำนวนผู้สมัคร ${ids.size} คน ผู้ได้รับการคัดเลือก¹ ${chosen.length} คน ผู้ชำระค่าลงทะเบียนแล้ว ${paid.length} คน</p><p>อัตราชำระค่าลงทะเบียนจากผู้ได้รับการคัดเลือก ${chosen.length?(paid.length/chosen.length*100).toFixed(1):'0.0'}% · อัตราสละสิทธิ์ ${chosen.length?(n(['42'])/chosen.length*100).toFixed(1):'0.0'}%</p><small>¹ รวมสถานะ 40–43 และ 50 เพื่อแสดงประวัติได้รับคัดเลือกจากสถานะปัจจุบัน ไม่ใช่สถิติขั้นตอนย้อนหลัง ส่วนสถานะก่อนหน้าต้องใช้ประวัติจาก API<br>ไม่มีข้อมูลปี 2567–2568 ในชุดปัจจุบัน จึงไม่แสดงแท่งและไม่คำนวณอัตราเติบโต การจัดกลุ่มสำนักวิชาอ้างอิงความสนใจ หนึ่งคนอาจอยู่หลายสำนักวิชา</small></div><p class="report-source">ขอบเขต ${esc(state.role==='teacher'?ownSchool().name:filters.school||'ทุกโรงเรียน')} · ปี ${esc(filters.year||'2569')} · ${esc(filters.round||'ทุกรอบ')} · ข้อมูลสาธิตสำหรับตรวจหน้าจอ</p>`;
 }
 function directorForm(){const d=read('director-'+ownSchool().schoolId,{});get('schoolEditor').innerHTML=`<form id="directorForm" class="school-stack">${select('คำนำหน้า','prefix',[d.prefix||'นาย','นาง','นางสาว'])}${field('ชื่อ','firstName',d.firstName||'','text',true)}${field('นามสกุล','lastName',d.lastName||'','text',true)}${field('ตำแหน่ง','position',d.position||'ผู้อำนวยการสถานศึกษา','text',true)}${field('วุฒิการศึกษาสูงสุด','qualification',d.qualification||'')}${field('เลขที่ใบอนุญาตประกอบวิชาชีพ','license',d.license||'')}${field('อีเมลติดต่อ','email',d.email||'','email',true)}${field('โทรศัพท์ติดต่อ','phone',d.phone||'','tel',true)}${field('วันที่เริ่มดำรงตำแหน่ง','startDate',d.startDate||'','date')}${field('วันที่สิ้นสุดวาระ','endDate',d.endDate||'','date')}${d.photoPreview?`<img class="director-photo" src="${esc(d.photoPreview)}" alt="รูปผู้อำนวยการ">`:''}${field('รูปถ่ายผู้อำนวยการ PNG/JPG ไม่เกิน 2 MB','photo','','file',false,'accept="image/png,image/jpeg"')}<label>หมายเหตุ<textarea name="note" rows="3">${esc(d.note||'')}</textarea></label><div class="form-actions">${btn('ยกเลิก','school-general')}<button class="btn btn-primary">ยืนยันข้อมูล</button></div></form>`;document.querySelectorAll('.school-tabs .tab').forEach(b=>b.classList.toggle('active',b.dataset.ui==='school-director'));}
 function schoolPage(){return backendTeacherSchoolPage();}

 // Restored reference components: scoped so existing screens remain unchanged.
 const profileTabs=active=>`<div class="tabs original-tabs" role="tablist" aria-label="ข้อมูลบัญชี"><button class="tab ${active==='profile'?'active':''}" role="tab" aria-selected="${active==='profile'}" data-ui="nav" data-target="profile">ข้อมูลทั่วไป</button><button class="tab ${active==='renew'?'active':''}" role="tab" aria-selected="${active==='renew'}" data-ui="renew-start">ต่ออายุการใช้งาน</button></div>`;
 const userName=()=>[state.profile.prefix,state.profile.firstName,state.profile.lastName].filter(Boolean).join(' ');
 function profilePage(){return heading('ข้อมูลส่วนตัวของฉัน','จัดการข้อมูลบัญชีและรูปโปรไฟล์ของท่าน')+profileTabs('profile')+`<div class="profile-delete-row">${btn('ลบข้อมูลผู้ใช้','delete-self','btn-primary')}</div><div class="profile-layout"><aside class="panel profile-identity"><div class="avatar avatar-large" id="profileAvatar">${state.profile.profilePicture?`<img src="${esc(state.profile.profilePicture)}" alt="รูปโปรไฟล์">`:'ค'}</div><div class="photo-actions"><label class="btn btn-outline photo-picker">เปลี่ยนรูปภาพ<input type="file" id="restoredPhoto" accept="image/png,image/jpeg"></label><button type="button" class="btn text-button" disabled title="ยังไม่เปิดใช้งานการลบรูป">ลบรูป</button></div><small class="muted">รองรับไฟล์ JPG/PNG ขนาดไม่เกิน 2 MB</small><h2>${esc(userName())}</h2><p>${esc(state.profile.positionTitle)}</p><p>${esc(ownSchool().name)}</p>${tag('ข้อมูลบัญชี')}<dl class="membership"><div><dt>สมาชิกตั้งแต่</dt><dd>20 เมษายน 2568</dd></div><div><dt>ปีการศึกษาที่ใช้งาน</dt><dd>${esc(state.profile.activeAcademicYear ?? '—')}</dd></div></dl>${btn('ต่ออายุการใช้งาน →','renew-start','text-button')}</aside><article class="panel profile-edit"><div class="section-caption"><h3>แก้ไขข้อมูลส่วนตัว</h3><small>จัดการข้อมูลส่วนตัว</small></div><form id="profileForm" class="form-grid"><label>
    คำนำหน้า
    <select name="prefixId" required
        ${state.profilePrefixes?.length ? '' : 'disabled'}>
        <option value="">เลือกคำนำหน้า</option>
        ${(state.profilePrefixes || []).map(prefix => `
            <option
                value="${prefix.prefixId}"
                ${prefix.prefixId === state.profilePrefixId
                    ? 'selected'
                    : ''}
            >
                ${esc(prefix.prefixName)}
            </option>
        `).join('')}
    </select>
</label>${field('ชื่อ','firstName',state.profile.firstName,'text',true,'maxlength="100"')}${field('นามสกุล','lastName',state.profile.lastName,'text',true,'maxlength="100"')}${field(
    'ตำแหน่ง',
    'positionTitle',
    state.profile.positionTitle,
    'text',
    false,
    'readonly maxlength="100"'
)}
${field(
    'อีเมล',
    'email',
    state.profile.email,
    'email',
    false,
    'readonly maxlength="150"'
)}<div class="full">
    ${btn('เปลี่ยนอีเมล', 'change-email', 'btn-outline')}
    ${state.profile.pendingEmail ? `<small class="muted">รอยืนยันอีเมล: ${esc(state.profile.pendingEmail)}</small>` : ''}
</div>${field('โทรศัพท์','phone',state.profile.phone,'tel',true,'maxlength="20"')}${field('โรงเรียนที่สังกัด (อ่านอย่างเดียว)','school',ownSchool().name,'text',false,'readonly')}${field('รหัสโรงเรียน (อ่านอย่างเดียว)','schoolCode',ownSchool().schoolCode,'text',false,'readonly')}<small class="full muted">หากต้องการแก้ไขโรงเรียนที่สังกัด กรุณาติดต่อผู้ดูแลระบบ</small><div class="form-actions full">${btn('ยกเลิก','nav','btn-outline','data-target="profile"')}<button class="btn btn-primary">บันทึกข้อมูล</button></div></form></article></div>`;}
 const lookupTabs=active=>`<div class="tabs original-tabs"><button class="tab ${active==='applicants'?'active':''}" data-ui="search-tab" data-tab="applicants">ข้อมูลผู้สมัคร</button><button class="tab ${active==='schools'?'active':''}" data-ui="search-tab" data-tab="schools">ข้อมูลโรงเรียน</button></div>`;
 function schoolLookup(){
    const school=ownSchool(),province=school.subdistrict?.district?.province?.name||'ยังไม่ระบุจังหวัด';
    return heading('ค้นหาข้อมูลโรงเรียน')+lookupTabs('schools')+panel(`<form id="ownSchoolFilter" class="lookup-filter form-grid">${select('จังหวัด','province',['ทุกจังหวัด',province])}${select('สังกัด','type',['ทั้งหมด (รัฐบาล / เอกชน / สาธิต)',school.schoolType||'ยังไม่ระบุสังกัด'])}<label class="lookup-query">ชื่อโรงเรียน<input name="query" type="search" placeholder="พิมพ์ชื่อโรงเรียน"></label><div class="form-actions">${btn('ล้างตัวกรอง','reset-school-lookup')}<button class="btn btn-primary">ค้นหา</button></div></form>`)+`<p class="list-caption">รายชื่อโรงเรียนที่คุณมีสิทธิ์เข้าถึง</p><div class="school-notice">ท่านสามารถดูข้อมูลผู้สมัครและเนื้อหาได้เฉพาะโรงเรียนของท่านเท่านั้น</div><div id="ownSchoolRows">${ownSchoolRows()}</div>`;
 }

 function showOwnSchoolDetails(){
    if(state.role!=='teacher')return;
    const school=ownSchool(),count=state.applicantOverview?.totalApplicants;
    openModal('รายละเอียดโรงเรียน',`<dl>
        <dt>ชื่อโรงเรียน</dt><dd>${esc(school.name||'—')}</dd>
        <dt>รหัสโรงเรียน</dt><dd>${esc(school.schoolCode||'—')}</dd>
        <dt>จังหวัด</dt><dd>${esc(school.subdistrict?.district?.province?.name||'ยังไม่ระบุจังหวัด')}</dd>
        <dt>สังกัด / ประเภทโรงเรียน</dt><dd>${esc(school.schoolType||'ยังไม่ระบุสังกัด')}</dd>
        <dt>จำนวนผู้สมัคร (ทุกปีการศึกษา)</dt><dd>${Number.isInteger(count)?count:'—'}</dd>
    </dl>`);
 }

 function ownSchoolRows(filters={}){
    const school=ownSchool(),province=school.subdistrict?.district?.province?.name||'ยังไม่ระบุจังหวัด',type=school.schoolType||'ยังไม่ระบุสังกัด';
    const match=(!filters.query||school.name.toLowerCase().includes(filters.query.trim().toLowerCase()))&&(!filters.province||filters.province==='ทุกจังหวัด'||filters.province===province)&&(!filters.type||filters.type.startsWith('ทั้งหมด')||filters.type===type);
    const count=state.applicantOverview?.totalApplicants;
    return panel(table(['ตรา','รหัสโรงเรียน','ชื่อโรงเรียน','จังหวัด','สังกัด','ผู้สมัคร (ทุกปี)','รายละเอียด'],match?[[`<span class="school-monogram">ร</span>`,esc(school.schoolCode||'—'),esc(school.name),esc(province),esc(type),Number.isInteger(count)?count:'—',btn('ดูรายละเอียด','teacher-school-applicants','btn-primary')]]:[]));
 }

 function ownSchoolStats(){return heading('ค้นหาข้อมูลโรงเรียน')+lookupTabs('schools')+`<div class="school-notice">ข้อมูลผู้สมัครเฉพาะ ${esc(ownSchool().name)} ปีการศึกษา 2569</div>`+panel(`<div class="section-caption"><div><h2>${esc(ownSchool().name)}</h2><small>รหัส ${esc(ownSchool().schoolCode)} · ${esc(ownSchool().subdistrict.district.province.name)} · ${esc(ownSchool().schoolType)}</small></div><div>${btn('ส่งออกรายชื่อ (CSV)','export-applicants')}${btn('พิมพ์สถิติ / PDF','print')}</div></div>`)+managementAnalytics();}
 function teacherReportPage(){return backendTeacherOverviewPage();}
 function reportInterestList(){const f=state.originalReportFilters||{};return scope(interests).filter(i=>(!f.year||i.applicant.academicYear===+f.year)&&(!f.term||f.term==='ทั้งหมด'||i.applicant.term===+f.term)&&(!f.faculty||f.faculty==='ทั้งหมด'||i.program.faculty===f.faculty)&&(!f.program||f.program==='ทั้งหมด'||i.program.programName===f.program)&&(!f.query||(i.applicant.firstName+' '+i.applicant.lastName+' '+i.applicant.applicantId).includes(f.query.trim())));}
 function reportApplicantList(){const f=state.originalReportFilters||{};const ints=reportInterestList(),ids=new Set(ints.map(i=>i.applicant.applicantId));return cohort(f).filter(s=>(!f.term||f.term==='ทั้งหมด'||s.applicant.term===+f.term)&&(!f.query||(s.applicant.firstName+' '+s.applicant.lastName+' '+s.applicant.applicantId).includes(f.query.trim()))&&((!f.faculty||f.faculty==='ทั้งหมด')&&(!f.program||f.program==='ทั้งหมด')||ids.has(s.applicant.applicantId)));}
 function originalReportResults(){return teacherOverviewResults();}

 const dateThai=year=>`31 พฤษภาคม ${year}`;
 // A receipt only checks this registration; it cannot verify or sign in.
 let registrationCheckToken = null;
 let registrationCheckTimer = null;
 let registrationCheckAbort = null;
 let registrationCheckGeneration = 0;
 function stopRegistrationCheck() {
    registrationCheckGeneration++;
    clearTimeout(registrationCheckTimer);
    registrationCheckAbort?.abort();
    registrationCheckAbort = null;
 }
 function startRegistrationCheck() {
    const generation = registrationCheckGeneration;
    if (!registrationCheckToken) return;
    const check = async () => {
        if (generation !== registrationCheckGeneration || state.auth !== 'sent') return;
        registrationCheckAbort = new AbortController();
        try {
            const response = await fetch('/api/auth/registration-verification', {
                method:'POST', headers:{'Content-Type':'application/json'},
                body:JSON.stringify({checkToken:registrationCheckToken}),
                cache:'no-store', signal:registrationCheckAbort.signal
            });
            if (generation !== registrationCheckGeneration || state.auth !== 'sent') return;
            if (response.status === 410) {
                try {sessionStorage.removeItem('mfu.registrationCheck');} catch {}
                registrationCheckToken = null;
                toast('การตรวจสอบอัตโนมัติหมดอายุ กรุณาเข้าสู่ระบบหลังยืนยันอีเมล');
                return;
            }
            if (response.ok) {
                const result = await response.json();
                if (generation !== registrationCheckGeneration || state.auth !== 'sent') return;
                if (result.verified === true) {
                    try {sessionStorage.removeItem('mfu.registrationCheck');} catch {}
                    registrationCheckToken = null;
                    auth('confirmed', 'teacher');
                    return;
                }
            }
        } catch (error) {
            if (error.name === 'AbortError') return;
        }
        if (generation === registrationCheckGeneration && state.auth === 'sent') {
            registrationCheckTimer = setTimeout(check, 4000);
        }
    };
    void check();
 }
 let registrationPending = false;
 const registrationProvince = school => school.provinceName || 'ไม่ระบุจังหวัด';
 async function startRegistration() {
    state.registrationDraft = {};
    state.passwordResetToken = null;
    state.registrationOptions = null;
    state.registrationOptionsLoading = true;
    state.registrationOptionsError = '';
    auth('register', 'teacher');
    const generation = loginGeneration;
    try {
        const response = await fetch('/api/auth/registration-options', {
            credentials: 'same-origin', headers: {'Accept': 'application/json'}
        });
        if (!response.ok) throw new Error('โหลดข้อมูลลงทะเบียนไม่สำเร็จ (' + response.status + ')');
        const options = await response.json();
        if (generation !== loginGeneration || state.auth !== 'register') return;
        if (!Array.isArray(options?.schools) || !Array.isArray(options.prefixes) ||
            !Number.isInteger(options.academicYear) ||
            !options.schools.every(s => Number.isInteger(s.schoolId) && typeof s.name === 'string') ||
            !options.prefixes.every(p => Number.isInteger(p.prefixId) && typeof p.prefixName === 'string')) {
            throw new Error('รูปแบบตัวเลือกลงทะเบียนไม่ถูกต้อง');
        }
        state.registrationOptions = options;
    } catch (error) {
        if (generation !== loginGeneration || state.auth !== 'register') return;
        state.registrationOptionsError = error.message || 'โหลดข้อมูลลงทะเบียนไม่สำเร็จ';
    } finally {
        if (generation === loginGeneration && state.auth === 'register') {
            state.registrationOptionsLoading = false;
            referenceAuth('register');
        }
    }
 }
 function registrationSchoolOptions(province) {
    return '<option value="">เลือกโรงเรียน</option>' +
        (state.registrationOptions?.schools || [])
        .filter(school => registrationProvince(school) === province)
        .map(school => `<option value="${school.schoolId}">${esc(school.name)}${school.schoolCode ? ' (' + esc(school.schoolCode) + ')' : ''}</option>`).join('');
 }
 function registrationFormMarkup() {
    if (state.registrationOptionsLoading) return '<h1>ลงทะเบียนครูแนะแนว</h1><p role="status">กำลังโหลดข้อมูลโรงเรียนและคำนำหน้า…</p>';
    if (!state.registrationOptions || state.registrationOptionsError) return `
        <h1>ลงทะเบียนครูแนะแนว</h1><p role="alert">${esc(state.registrationOptionsError || 'ยังไม่มีข้อมูลลงทะเบียน')}</p>
        ${btn('ลองอีกครั้ง','registration-retry','btn-primary')}`;
    const options = state.registrationOptions;
    if (!options.schools.length || !options.prefixes.length) return `
        <h1>ลงทะเบียนครูแนะแนว</h1><p>ยังไม่มีโรงเรียนที่เปิดรับลงทะเบียนหรือข้อมูลคำนำหน้า กรุณาติดต่อผู้ดูแลระบบ</p>`;
    const provinces = [...new Set(options.schools.map(registrationProvince))].sort();
    return `<h1>ลงทะเบียนครูแนะแนว</h1><p class="reference-subtitle">ปีการศึกษา ${options.academicYear} · หนึ่งบัญชีต่อหนึ่งโรงเรียน</p>
        <form id="authForm" class="reference-form">
            <div class="reference-row">
                <label>จังหวัดของโรงเรียน<select name="province" required><option value="">เลือกจังหวัด</option>${provinces.map(p => `<option value="${esc(p)}">${esc(p)}</option>`).join('')}</select></label>
                <label>โรงเรียน<select name="schoolId" required disabled><option value="">เลือกโรงเรียน</option></select></label>
            </div>
            <div class="reference-name-row">
                <label>คำนำหน้า<select name="prefixId" required><option value="">เลือก</option>${options.prefixes.map(p => `<option value="${p.prefixId}">${esc(p.prefixName)}</option>`).join('')}</select></label>
                ${referenceField('ชื่อ','firstName','','text','กรอกชื่อจริง').replace('<input ', '<input maxlength="100" ')}
                ${referenceField('นามสกุล','lastName','','text','กรอกนามสกุล').replace('<input ', '<input maxlength="100" ')}
            </div>
            <div class="reference-row">
                ${referenceField('ตำแหน่ง','positionTitle','ครูแนะแนว').replace('<input ', '<input readonly ')}
                ${referenceField('เบอร์โทรศัพท์','phone','','tel','เช่น 0800000000').replace('<input ', '<input pattern="0[0-9]{9}" maxlength="10" ')}
            </div>
            ${referenceField('อีเมล','email','','email','example@yourdomain.com').replace('<input ', '<input maxlength="150" ')}
            <button type="submit" class="btn btn-primary reference-submit">ถัดไป: ตั้งรหัสผ่าน</button>
        </form>`;
 }
 function restoreRegistrationDraft() {
    const form = get('authForm');
    const draft = state.registrationDraft;
    if (!form || !draft || !state.registrationOptions) return;
    const school = state.registrationOptions.schools.find(s => s.schoolId === draft.schoolId);
    if (school) {
        const province = registrationProvince(school);
        form.elements.province.value = province;
        form.elements.schoolId.innerHTML = registrationSchoolOptions(province);
        form.elements.schoolId.disabled = false;
        form.elements.schoolId.value = String(school.schoolId);
    }
    for (const key of ['prefixId','firstName','lastName','phone','email']) {
        if (draft[key] != null) form.elements[key].value = String(draft[key]);
    }
 }
 function prepareRegistration(data) {
    const options = state.registrationOptions;
    const schoolId = Number(data.schoolId), prefixId = Number(data.prefixId);
    if (!options?.schools.some(s => s.schoolId === schoolId) ||
        !options.prefixes.some(p => p.prefixId === prefixId)) {
        toast('กรุณาเลือกโรงเรียนและคำนำหน้าจากรายการ'); return;
    }
    const firstName = (data.firstName || '').trim();
    const lastName = (data.lastName || '').trim();
    const phone = (data.phone || '').trim();
    const email = (data.email || '').trim().toLowerCase();
    if (!firstName || !lastName || firstName.length > 100 || lastName.length > 100 ||
        !/^0[0-9]{9}$/.test(phone) || email.length > 150 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        toast('กรุณาตรวจสอบชื่อ-นามสกุล อีเมล และเบอร์โทรศัพท์ 10 หลัก'); return;
    }
    state.registrationDraft = {schoolId, prefixId, firstName, lastName, phone, email};
    auth('password', 'teacher');
 }
 async function submitRegistration(form, data) {
    if (registrationPending || state.passwordResetToken) return;
    if (!state.registrationDraft?.schoolId) {
        toast('กรุณากรอกข้อมูลลงทะเบียนก่อน'); return;
    }
    if (data.password !== data.confirm) {
        form.elements.confirm.setCustomValidity('รหัสผ่านไม่ตรงกัน');
        form.elements.confirm.reportValidity(); return;
    }
    if (!data.password || data.password.length < 8 || new TextEncoder().encode(data.password).length > 72) {
        toast('รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร และไม่เกิน 72 ไบต์'); return;
    }
    if (data.pdpaConsent !== 'on') {
        toast('กรุณายอมรับการเก็บและใช้ข้อมูลก่อนลงทะเบียน'); return;
    }
    const draft = state.registrationDraft;
    const generation = loginGeneration;
    const button = form.querySelector('button[type="submit"], button.btn-primary');
    registrationPending = true;
    if (button) {button.disabled = true; button.textContent = 'กำลังส่งคำขอ…';}
    try {
        const registration = await window.MfuApi.request('GuidanceTeacherController.create', {
            auth: false,
            body: {
                school: {schoolId: draft.schoolId},
                prefix: {prefixId: draft.prefixId},
                firstName: draft.firstName, lastName: draft.lastName,
                positionTitle: 'ครูแนะแนว', phone: draft.phone, email: draft.email,
                passwordHash: data.password, pdpaConsent: true
            }
        });
        if (generation !== loginGeneration || get('authForm') !== form) return;
        form.elements.password.value = '';
        form.elements.confirm.value = '';
        state.registrationDraft = null;
        try { sessionStorage.setItem('mfu.registrationCheck', JSON.stringify({token:registration.verificationCheckToken, expiresAt:Date.now()+86400000})); } catch {}
        registrationCheckToken = registration.verificationCheckToken || null;
        auth('sent', 'teacher');
    } catch (error) {
        if (generation === loginGeneration && get('authForm') === form) {
            toast((error.message || 'ลงทะเบียนไม่สำเร็จ') + ' หากส่งคำขอแล้ว กรุณาตรวจสอบอีเมลก่อนลองซ้ำ');
        }
    } finally {
        registrationPending = false;
        if (button?.isConnected) {button.disabled = false; button.textContent = 'ยินยอมและยื่นคำขอ';}
    }
 }
 document.addEventListener('input', e => {
    if (e.target.closest('#authForm') && ['password','confirm'].includes(e.target.name)) {
        const confirm = get('authForm')?.elements.confirm;
        if (confirm) confirm.setCustomValidity('');
    }
 });

 let renewalPending = false;
 function applyRenewalProfile(profile) {
    state.profilePrefixId = profile.prefix?.prefixId ?? null;
    state.profile = {
        ...state.profile, ...profile,
        schoolId: profile.school.schoolId,
        prefix: profile.prefix?.prefixName || ''
    };
    const index = schools.findIndex(s => s.schoolId === profile.school.schoolId);
    if (index < 0) schools.push(profile.school);
    else schools[index] = profile.school;
 }
 async function startRenewal() {
    if (state.role !== 'teacher' || renewalPending) return;
    renewalPending = true;
    const generation = loginGeneration;
    const teacherId = state.profile.teacherId;
    try {
        const profile = await window.MfuApi.request('GuidanceTeacherController.getMyProfile');
        if (generation !== loginGeneration || state.role !== 'teacher') return;
        if (profile?.teacherId !== teacherId || !profile.school?.schoolId ||
            !Number.isInteger(profile.activeAcademicYear)) {
            throw new Error('ข้อมูลบัญชีหรือปีการศึกษาไม่ครบ กรุณาตรวจสอบกับผู้ดูแลระบบ');
        }
        applyRenewalProfile(profile);
        state.renewDraft = null;
        state.renewal = 1;
        render('renew');
    } catch (error) {
        if (generation === loginGeneration) toast(error.message || 'โหลดข้อมูลต่ออายุไม่สำเร็จ');
    } finally { renewalPending = false; }
 }
 function prepareRenewal(data) {
    if (state.role !== 'teacher' || renewalPending) return;
    const prefixId = Number(data.prefixId);
    if (!(state.profilePrefixes || []).some(p => p.prefixId === prefixId)) {
        toast('กรุณาเลือกคำนำหน้าจากรายการ'); return;
    }
    const school = state.profile.school;
    if (!school?.schoolId || !school.name || !school.schoolCode ||
        !Number.isInteger(state.profile.activeAcademicYear)) {
        toast('ข้อมูลโรงเรียนหรือปีการศึกษาไม่ครบ กรุณาติดต่อผู้ดูแลระบบ'); return;
    }
    if (state.profile.isEmailVerified !== true) {
        toast('กรุณายืนยันอีเมลก่อนต่ออายุ'); return;
    }
    const teacher = {
        prefix: {prefixId},
        firstName: (data.firstName || '').trim(),
        lastName: (data.lastName || '').trim(),
        phone: (data.phone || '').trim() || null,
        positionTitle: (data.positionTitle || '').trim(),
        school: {
            name: school.name,
            schoolCode: school.schoolCode,
            schoolType: (data.schoolType || '').trim()
        }
    };
    if (!teacher.firstName || !teacher.lastName || !teacher.positionTitle || !teacher.school.schoolType) {
        toast('กรุณากรอกข้อมูลส่วนตัว ตำแหน่ง และประเภทโรงเรียนให้ครบ'); return;
    }
    if (teacher.phone && !/^0[0-9]{9}$/.test(teacher.phone)) {
        toast('เบอร์โทรศัพท์ต้องมี 10 หลักและเริ่มต้นด้วย 0'); return;
    }
    state.renewDraft = {
        teacher,
        targetAcademicYear: state.profile.activeAcademicYear + 1
    };
    openModal('ยืนยันการต่ออายุการใช้งาน', `
        <p>ปีการศึกษาปัจจุบัน ${state.profile.activeAcademicYear} → ${state.renewDraft.targetAcademicYear}</p>
        <p>${esc(teacher.firstName)} ${esc(teacher.lastName)} · ${esc(school.name)}</p>
        <p>การยืนยันจะบันทึกข้อมูลและปีการศึกษาใหม่ลงฐานข้อมูลทันที</p>
        <label class="check"><input type="checkbox" id="renewConsent"> ข้าพเจ้าตรวจสอบข้อมูลและยืนยันการต่ออายุ</label>
    `, btn('กลับไปตรวจสอบ','close') + btn('ยืนยัน','renew-confirm','btn-primary','id="renewConfirm" disabled'));
 }
 async function confirmRenewal(button) {
    if (renewalPending || state.role !== 'teacher' || !state.renewDraft ||
        !get('renewConsent')?.checked) return;
    const body = state.renewDraft;
    const generation = loginGeneration;
    const teacherId = state.profile.teacherId;
    const current = () => generation === loginGeneration && state.role === 'teacher' &&
        state.profile.teacherId === teacherId;
    renewalPending = true;
    button.disabled = true;
    button.textContent = 'กำลังบันทึก…';
    try {
        // Recheck before writing, including retries after an uncertain response.
        const fresh = await window.MfuApi.request('GuidanceTeacherController.getMyProfile');
        if (!current()) return;
        if (fresh?.teacherId !== teacherId || !fresh.school?.schoolId) throw new Error('ข้อมูลบัญชีไม่ครบ');
        if (fresh.activeAcademicYear === body.targetAcademicYear) {
            applyRenewalProfile(fresh);
        } else {
            if (fresh.activeAcademicYear !== body.targetAcademicYear - 1) {
                throw new Error('ปีการศึกษาเปลี่ยนแล้ว กรุณาปิดและเริ่มขั้นตอนต่ออายุใหม่');
            }
            const result = await window.MfuApi.request('GuidanceTeacherController.renewMyAcademicYear', {body});
            if (!current()) return;
            if (result?.activeAcademicYear !== body.targetAcademicYear) {
                throw new Error('ยังยืนยันผลไม่ได้ กรุณาลองอีกครั้งเพื่อตรวจสอบปีการศึกษาก่อนบันทึกซ้ำ');
            }
            const prefix = state.profilePrefixes.find(p => p.prefixId === body.teacher.prefix.prefixId);
            applyRenewalProfile({
                ...fresh,
                ...body.teacher,
                prefix,
                school: {...fresh.school, ...body.teacher.school},
                activeAcademicYear: result.activeAcademicYear
            });
        }
        state.renewDraft = null;
        state.renewal = 4;
        // These charts were loaded for the old active year; do not mislabel them.
        state.applicantRoundStatistics = null;
        state.applicantProgramStatistics = null;
        sessionStorage.removeItem('mfu.school');
        if (get('uiDialog').open) get('uiDialog').close();
        render('renew');
        toast('ต่ออายุและบันทึกปีการศึกษาลงฐานข้อมูลแล้ว');
    } catch (error) {
        if (current()) toast(error.message || 'ต่ออายุไม่สำเร็จ กรุณาลองอีกครั้ง');
    } finally {
        renewalPending = false;
        if (button.isConnected) {
            button.textContent = 'ยืนยัน';
            button.disabled = !get('renewConsent')?.checked;
        }
    }
 }
 function renewal() {
    const step = state.renewal;
    const year = state.profile.activeAcademicYear;
    const next = step === 4 ? year : year + 1;
    const school = state.profile.school || {};
    const labels = ['ตรวจสอบข้อมูล','ตรวจสอบบัญชี','ยืนยัน/อัปเดตข้อมูล','ต่ออายุสำเร็จ'];
    const common = heading('ต่ออายุการใช้งาน', `ปีการศึกษา ${next}`) + profileTabs('renew') + `
        <div class="school-notice renewal-expiry">ปีการศึกษาที่ใช้งาน: ${esc(year)}</div>
        <ol class="renewal-steps">${labels.map((label,index) => `
            <li class="${step === index + 1 ? 'active' : step > index + 1 ? 'done' : ''}">
                <span>${step > index + 1 ? '✓' : index + 1}</span><b>${label}</b>
            </li>`).join('')}</ol>`;
    const side = `<h3>สรุปการต่ออายุ</h3><div class="renewal-summary"><small>ปีการศึกษา</small><strong>${esc(next)}</strong></div>
        <p>ต่ออายุได้ครั้งละหนึ่งปีการศึกษา</p><p class="muted">ปีการศึกษาใหม่จะถูกบันทึกเมื่อกดยืนยันสำเร็จ</p>`;
    let main;
    if (step === 1) {
        main = `<h3>ตรวจสอบข้อมูลก่อนต่ออายุ</h3><dl class="review-grid">
            ${[['ชื่อ-นามสกุล',userName()],['โรงเรียน',school.name],['รหัสโรงเรียน',school.schoolCode],['อีเมล',state.profile.email],['โทรศัพท์',state.profile.phone],['ปีการศึกษาปัจจุบัน',year]].map(([k,v]) => `<div><dt>${esc(k)}</dt><dd>${esc(v ?? '—')}</dd></div>`).join('')}
            </dl><div class="form-actions">${btn('ยกเลิก','nav','btn-outline','data-target="profile"')}${btn('ถัดไป: ตรวจสอบบัญชี','renew-next','btn-primary')}</div>`;
    } else if (step === 2) {
        const verified = state.profile.isEmailVerified === true;
        main = `<h3>ตรวจสอบบัญชีผู้ใช้งาน</h3><p>บัญชี: ${esc(state.profile.email)}</p>
            <div class="school-notice">${verified ? 'บัญชีนี้ยืนยันอีเมลแล้ว' : 'ต้องยืนยันอีเมลก่อนต่ออายุ'}</div>
            <p>ใช้บัญชีที่เข้าสู่ระบบและสถานะการยืนยันอีเมลเพื่อต่ออายุ</p>
            <div class="form-actions">${btn('ย้อนกลับ','renew-back')}${btn('ถัดไป: ตรวจสอบข้อมูล','renew-next','btn-primary',verified ? '' : 'disabled')}</div>`;
    } else if (step === 3) {
        const draft = state.renewDraft?.teacher || {};
        const prefixId = draft.prefix?.prefixId ?? state.profilePrefixId;
        main = `<h3>ยืนยัน/อัปเดตข้อมูล</h3><form id="renewForm" class="form-grid renewal-form">
            <label>คำนำหน้า<select name="prefixId" required><option value="">เลือกคำนำหน้า</option>${(state.profilePrefixes || []).map(p => `<option value="${p.prefixId}" ${p.prefixId === prefixId ? 'selected' : ''}>${esc(p.prefixName)}</option>`).join('')}</select></label>
            ${field('ชื่อ','firstName',draft.firstName ?? state.profile.firstName,'text',true,'maxlength="100"')}
            ${field('นามสกุล','lastName',draft.lastName ?? state.profile.lastName,'text',true,'maxlength="100"')}
            ${field('ตำแหน่ง','positionTitle',draft.positionTitle ?? state.profile.positionTitle,'text',true,'maxlength="100"')}
            ${field('โทรศัพท์','phone',draft.phone ?? state.profile.phone ?? '','tel',false,'pattern="0[0-9]{9}" maxlength="10"')}
            ${field('อีเมล','email',state.profile.email,'email',false,'readonly')}
            ${field('โรงเรียน','schoolName',school.name,'text',false,'readonly')}
            ${field('รหัสโรงเรียน','schoolCode',school.schoolCode,'text',false,'readonly')}
            ${field('ประเภทโรงเรียน','schoolType',draft.school?.schoolType ?? school.schoolType ?? '','text',true,'maxlength="50"')}
            <div class="form-actions full">${btn('ย้อนกลับ','renew-back')}<button type="submit" class="btn btn-primary">ตรวจสอบและต่ออายุ</button></div>
        </form>`;
    } else {
        main = `<div class="success-state"><span>✓</span><h2>ต่ออายุสำเร็จ</h2><p>ปีการศึกษาที่บันทึก: ${esc(year)}</p></div>
            <p>กรุณาออกจากระบบและเข้าสู่ระบบใหม่เพื่อโหลดสถิติของปีการศึกษาใหม่</p>
            <div class="form-actions">${btn('พิมพ์หลักฐาน / PDF','print')}${btn('กลับข้อมูลส่วนตัว','nav','btn-primary','data-target="profile"')}</div>`;
    }
    return common + `<div class="renewal-layout"><article class="panel renewal-main">${main}</article><aside class="panel renewal-aside">${side}</aside></div>`;
 }
 function autoRenewToggle(){return `<div class="auto-renew-row"><span>ต่ออายุอัตโนมัติ</span><label class="switch"><input type="checkbox" id="autoRenew" ${read('autoRenew',false)?'checked':''}><span></span></label></div><small class="muted">ตั้งค่าตัวอย่างในแท็บนี้</small>`;}
 function autoRenewPanel(){return `<h3>ต่ออายุอัตโนมัติ</h3>${autoRenewToggle()}<div class="school-notice">ระบบจะแจ้งเตือนก่อนหมดอายุ 60 วัน</div><h3>ความปลอดภัยของข้อมูล</h3><p class="muted">ข้อมูลส่วนบุคคลใช้เพื่อยืนยันสิทธิ์การใช้งานเท่านั้น และดูแลตามนโยบาย PDPA</p><div class="renewal-summary">ไม่เปิดเผยรหัส OTP แก่ผู้อื่น</div>`;}

 document.addEventListener('input',e=>{if(e.target.classList.contains('otp-digit')){e.target.value=e.target.value.replace(/[^0-9]/g,'').slice(-1);const inputs=[...document.querySelectorAll('.otp-digit')];get('combinedOtp').value=inputs.map(i=>i.value).join('');if(e.target.value)inputs[Number(e.target.dataset.index)+1]?.focus();}});

 // Admin reference pages: each uses the existing PDF's content hierarchy.
 // Print a dedicated document in an off-screen frame, without opening a tab.
 function createReportPrintFrame() {
    document.getElementById('mfuReportPrintFrame')?.remove();
    const frame=document.createElement('iframe');
    frame.id='mfuReportPrintFrame';
    frame.title='เอกสารรายงานสำหรับพิมพ์';
    frame.setAttribute('aria-hidden','true');
    frame.tabIndex=-1;
    frame.style.cssText='position:fixed;left:-10000px;top:0;width:1px;height:1px;border:0;pointer-events:none;';
    document.body.appendChild(frame);
    return frame.contentWindow;
 }
 let teacherApplicantRequest=0;
 async function loadTeacherApplicantSearch(){
    if(state.role!=='teacher')return;
    const request=++teacherApplicantRequest,generation=loginGeneration,schoolId=state.profile.schoolId;
    const current=()=>request===teacherApplicantRequest&&generation===loginGeneration&&state.role==='teacher'&&state.profile.schoolId===schoolId;
    const filters=state.teacherApplicantFilters||{},year=Number(filters.year)||state.profile.activeAcademicYear;
    if(!Number.isInteger(year)){state.teacherApplicantLoaded=true;state.teacherApplicantError='ยังไม่ได้กำหนดปีการศึกษา';return;}
    const catalog=state.teacherApplicantCatalog||[];
    if(catalog.length){
        const matching=catalog.filter(p=>Number(p.academicYear||p.entryGroup?.academicYear)===year);
        if(filters.round&&!matching.some(p=>(p.round||p.entryGroup?.entrygroupName)===filters.round))filters.round='';
        if(filters.program&&!matching.some(p=>String(p.programId)===String(filters.program)&&(!filters.round||(p.round||p.entryGroup?.entrygroupName)===filters.round)))filters.program='';
    }
    state.teacherApplicantFilters=filters;
    state.teacherApplicantLoaded=true;state.teacherApplicantLoading=true;state.teacherApplicantError='';
    if(state.page==='applicants')render('applicants');
    try{
        const query={academicYear:year};if(filters.round)query.admissionRound=filters.round;if(filters.program)query.programId=Number(filters.program);
        const responses=await Promise.allSettled([
            window.MfuApi.request('SchoolStatisticsController.getStatistics',{query}),
            state.teacherApplicantCatalog?.length?Promise.resolve(state.teacherApplicantCatalog):window.MfuApi.request('ProgramController.getAll')
        ]);
        if(!current())return;
        if(responses[0].status==='rejected')throw responses[0].reason;
        const report=responses[0].value;
        if(!Array.isArray(report?.applicants))throw Error('รูปแบบข้อมูลผู้สมัครไม่ถูกต้อง');
        state.teacherApplicantReport=report;
        if(responses[1].status==='fulfilled'&&Array.isArray(responses[1].value))state.teacherApplicantCatalog=responses[1].value;
        state.teacherApplicantPage=0;updateTeacherApplicantPage();
    }catch(error){if(current())state.teacherApplicantError=error.message||'ค้นหาผู้สมัครไม่สำเร็จ';}
    finally{if(current()){state.teacherApplicantLoading=false;if(state.page==='applicants')render('applicants');}}
 }
 function updateTeacherApplicantPage(){
    const query=String(state.teacherApplicantFilters?.query||'').trim().toLowerCase();
    const rows=(state.teacherApplicantReport?.applicants||[]).filter(r=>!query||[r.fullName,r.studentId,String(r.applicantId)].some(v=>String(v||'').toLowerCase().includes(query)));
    const totalPages=Math.ceil(rows.length/5),page=Math.max(0,Math.min(Number(state.teacherApplicantPage)||0,Math.max(0,totalPages-1)));
    state.teacherApplicantPage=page;
    state.applicantSearchKeyword=query;
    state.applicantSearchResult={content:rows.slice(page*5,page*5+5).map(r=>({applicantId:r.applicantId,studentName:r.fullName,schoolName:ownSchool().name,statusDescription:r.statusDescription||'ยังไม่มีผลที่เผยแพร่',studentId:r.studentId})),currentPage:page,pageSize:5,totalPages,totalItems:rows.length};
 }
 function teacherApplicantFiltersMarkup(){
    const f=state.teacherApplicantFilters||{},catalog=state.teacherApplicantCatalog||[];
    const year=String(f.year||state.profile.activeAcademicYear||'');
    const years=[...new Set([year,...catalog.map(p=>String(p.academicYear||p.entryGroup?.academicYear||''))].filter(Boolean))].sort((a,b)=>Number(b)-Number(a));
    const programs=catalog.filter(p=>String(p.academicYear||p.entryGroup?.academicYear)===year);
    const rounds=[...new Set([f.round,...programs.map(p=>p.round||p.entryGroup?.entrygroupName)].filter(Boolean))].sort();
    const available=programs.filter(p=>!f.round||(p.round||p.entryGroup?.entrygroupName)===f.round);
    const disabled=state.teacherApplicantLoading?'disabled':'';
    return `<form id="applicantFilter" class="filter-panel flat-filter">
        <label>รอบการสมัคร TCAS<select name="round"><option value="">ทุกรอบ</option>${rounds.map(r=>`<option value="${esc(r)}" ${r===f.round?'selected':''}>${esc(r)}</option>`).join('')}</select></label>
        <label>ปีการศึกษา<select name="year">${years.map(y=>`<option value="${y}" ${y===year?'selected':''}>${y}</option>`).join('')}</select></label>
        <label>หลักสูตรที่สนใจ<select name="program"><option value="">ทั้งหมด</option>${available.map(p=>`<option value="${p.programId}" ${String(p.programId)===f.program?'selected':''}>${esc(p.programName)}</option>`).join('')}</select></label>
        ${field('ชื่อ–สกุล / เลขประจำตัวประชาชน','query',f.query||'','search')}
        <button class="btn btn-outline" type="reset">ล้างตัวกรอง</button><button class="btn btn-primary" type="submit" ${disabled}>ค้นหา</button>
    </form>`;
 }
 let teacherReportRequest = 0;
 function teacherReportFilterValues() {
    const f=state.originalReportFilters||{};
    return {term:f.term||'ทั้งหมด',year:f.year===undefined?String(state.profile.activeAcademicYear||2569):f.year,
        faculty:f.faculty||'ทั้งหมด',program:f.program||'ทั้งหมด',query:f.query||''};
 }
 async function loadTeacherOverviewReport() {
    if(state.role!=='teacher')return;
    const request=++teacherReportRequest,generation=loginGeneration,schoolId=state.profile.schoolId;
    const mode=state.reportTab||'student',filters=teacherReportFilterValues();
    const current=()=>request===teacherReportRequest&&generation===loginGeneration&&state.role==='teacher'&&state.profile.schoolId===schoolId;
    state.teacherReportLoaded=true;state.teacherReportLoading=true;state.teacherReportError='';
    if(state.page==='overview')render('overview');
    const query={};
    if(filters.term!=='ทั้งหมด')query.semester=Number(filters.term);
    if(filters.year&&filters.year!=='ทั้งหมด')query.academicYear=Number(filters.year);
    if(filters.faculty!=='ทั้งหมด')query.faculty=filters.faculty;
    if(filters.query.trim())query.keyword=filters.query.trim();
    async function studentRows() {
        const rows=[];let page=0,totalPages=1;
        while(page<totalPages){
            const response=await window.MfuApi.request('StudentInterestReportController.getReport',{query:{...query,...(filters.program!=='ทั้งหมด'?{major:filters.program}:{}),page,size:100}});
            if(!current())return null;
            if(!Array.isArray(response?.content)||!Number.isInteger(response.totalPages)||response.totalPages<0)throw Error('รูปแบบรายงานความสนใจไม่ถูกต้อง');
            rows.push(...response.content);totalPages=response.totalPages;page++;
        }
        return rows;
    }
    try {
        const programsPromise=state.teacherReportPrograms?.length?Promise.resolve(state.teacherReportPrograms):
            window.MfuApi.request('ProgramController.getAll').catch(()=>[]);
        let rows;
        if(mode==='applicants'){
            const report=await window.MfuApi.request('ApplicantOverviewReportController.getReport',{query});
            if(!current())return;
            if(!Array.isArray(report?.applicants))throw Error('รูปแบบรายงานผู้สมัครไม่ถูกต้อง');
            rows=report.applicants;
            if(filters.program!=='ทั้งหมด'){
                const interests=await studentRows();if(!current())return;
                const ids=new Set(interests.map(i=>i.applicantId));rows=rows.filter(a=>ids.has(a.applicantId));
            }
        }else{
            rows=await studentRows();if(!current())return;
            if(filters.program!=='ทั้งหมด')rows=rows.filter(r=>r.major===filters.program);
        }
        const catalog=await programsPromise;
        if(!current())return;
        state.teacherReportPrograms=Array.isArray(catalog)?catalog:[];
        state.teacherReportData={mode,filters,rows,schoolName:state.profile.school?.name||ownSchool().name};
        state.reportListPage=1;
    }catch(error){if(current())state.teacherReportError=error.message||'โหลดรายงานไม่สำเร็จ';}
    finally{if(current()){state.teacherReportLoading=false;if(state.page==='overview')render('overview');}}
 }
 function teacherReportSnapshot() {
    const data=state.teacherReportData;if(!data)return null;
    const titles={student:'รายงานความสนใจของนักเรียน',projects:'รายงานสถิติความสนใจโครงการ',applicants:'รายงานภาพรวมผู้สมัคร'};
    let headers,rows,summary=[];
    if(data.mode==='student'){
        headers=['ลำดับ','ชื่อ–นามสกุล','สำนักวิชา','สาขา','เทอม','ปีการศึกษา','สถานะ'];
        rows=data.rows.map((r,i)=>[i+1,r.fullName,r.faculty,r.major,r.semester,r.academicYear,r.status||'สนใจ']);
    }else if(data.mode==='projects'){
        const groups=new Map();
        for(const r of data.rows){const key=JSON.stringify([r.faculty,r.major,r.semester,r.academicYear]);if(!groups.has(key))groups.set(key,{name:r.major,faculty:r.faculty,term:r.semester,year:r.academicYear,count:0});groups.get(key).count++;}
        const programs=[...groups.values()].sort((a,b)=>b.count-a.count||String(a.name).localeCompare(String(b.name)));
        headers=['ลำดับ','ชื่อหลักสูตร / โครงการ','สำนักวิชา','จำนวนความสนใจ','เทอม','ปีการศึกษา'];
        rows=programs.map((r,i)=>[i+1,r.name,r.faculty,r.count,r.term,r.year]);
        summary=[['จำนวนผู้สมัคร',new Set(data.rows.map(r=>r.applicantId)).size],['จำนวนหลักสูตร',new Set(data.rows.map(r=>JSON.stringify([r.faculty,r.major]))).size],['จำนวนความสนใจ',data.rows.length]];
    }else{
        const count=codes=>data.rows.filter(r=>codes.includes(String(r.statusCode))).length;
        summary=[['จำนวนผู้สมัคร',data.rows.length],['ผู้มีสิทธิ์สอบคัดเลือก',count(['20','21','22','23','24','25','26','27','28'])],['ผู้ผ่านการคัดเลือก',count(['40','41','42','43','50'])],['ผู้ชำระค่าลงทะเบียน',count(['50'])],['ผู้สละสิทธิ์',count(['42'])],['ผู้ไม่ยืนยันทะเบียน',count(['41'])]];
        headers=['รหัสผู้สมัคร','ชื่อ–นามสกุล','โรงเรียน','เทอม','ปีการศึกษา','สถานะ'];
        rows=data.rows.map(r=>[r.applicantId,r.fullName,r.schoolName,r.semester,r.academicYear,r.statusDescription||'ยังไม่มีผลที่เผยแพร่']);
    }
    return {title:titles[data.mode],schoolName:data.schoolName,filters:data.filters,headers,rows,summary};
 }
 function backendTeacherOverviewPage() {
    const mode=state.reportTab||'student',f=teacherReportFilterValues(),catalog=state.teacherReportPrograms||[];
    const titles={student:'รายงานความสนใจของนักเรียน',projects:'รายงานสถิติความสนใจโครงการ',applicants:'รายงานภาพรวมผู้สมัคร'};
    const dropdown=(label,name,items,value)=>`<label>${label}<select name="${name}">${[...new Set([value,...items].filter(v=>v!==undefined&&v!==null&&v!==''))].map(v=>`<option value="${esc(v)}" ${String(v)===String(value)?'selected':''}>${esc(v)}</option>`).join('')}</select></label>`;
    const tabs=`<div class="tabs original-tabs report-tabs">${[['student','ความสนใจของนักเรียน'],['projects','สถิติความสนใจโครงการ'],['applicants','ภาพรวมผู้สมัคร']].map(([key,label])=>`<button class="tab ${mode===key?'active':''}" data-ui="report-tab" data-tab="${key}">${label}</button>`).join('')}</div>`;
    const years=[state.profile.activeAcademicYear,...catalog.map(p=>p.academicYear)].filter(Boolean).map(String).sort((a,b)=>Number(b)-Number(a));
    const filters=panel(`<form id="originalReportFilter" class="original-report-filter">
        ${dropdown('เทอม','term',['ทั้งหมด','1','2'],f.term)}
        ${dropdown('ปีการศึกษา','year',['ทั้งหมด',...years],f.year)}
        ${dropdown('สำนักวิชา','faculty',['ทั้งหมด',...catalog.map(p=>p.faculty).filter(Boolean)],f.faculty)}
        ${dropdown('สาขา','program',['ทั้งหมด',...catalog.map(p=>p.programName).filter(Boolean)],f.program)}
        ${field('รายชื่อผู้สมัคร','query',f.query,'search',false,'placeholder="ค้นหาชื่อ–นามสกุล หรือเลขบัตรประชาชน"')}
        <div class="form-actions full"><button type="reset" class="btn btn-outline">ล้างตัวกรอง</button><button class="btn btn-primary" ${state.teacherReportLoading?'disabled':''}>ค้นหา</button></div>
    </form>`);
    return heading(titles[mode])+tabs+filters+`<div id="originalReportResults">${teacherOverviewResults()}</div>`;
 }
 function teacherOverviewResults() {
    if(state.teacherReportLoading)return panel('<p role="status">กำลังโหลดรายงาน…</p>');
    if(state.teacherReportError)return panel(`<p role="alert">${esc(state.teacherReportError)}</p>${btn('ลองอีกครั้ง','teacher-report-retry','btn-primary')}`);
    const report=teacherReportSnapshot();if(!report)return '';
    const total=report.rows.length,pages=Math.max(1,Math.ceil(total/5)),page=Math.min(Math.max(1,state.reportListPage||1),pages);
    state.reportListPage=page;
    const summary=report.summary.length?`<div class="kpi-grid">${report.summary.map(([name,count])=>`<div class="panel kpi"><span>${esc(name)}</span><strong>${count}</strong></div>`).join('')}</div>`:'';
    const rows=report.rows.slice((page-1)*5,page*5);
    return panel(`<div class="section-caption"><h3>ผลการค้นหา</h3>${btn('พิมพ์รายงาน','print')}</div>${summary}${table(report.headers,rows.map(row=>row.map(esc)))}
        <div class="pagination"><span>แสดง ${total?(page-1)*5+1:0}–${Math.min(page*5,total)} จาก ${total} รายการ</span><div>${Array.from({length:pages},(_,i)=>btn(String(i+1),'report-list-page',i+1===page?'btn-primary':'btn-outline',`data-number="${i+1}"`)).join('')}</div></div>
        <p class="report-source">${esc(report.schoolName)}</p>`);
 }
 function printTeacherOverviewReport() {
    if(state.teacherReportLoading||state.teacherReportError)return;
    const report=teacherReportSnapshot();if(!report){toast('กรุณารอให้รายงานโหลดเสร็จ');return;}
    const w=createReportPrintFrame();if(!w){toast('ไม่สามารถเตรียมเอกสารสำหรับพิมพ์ได้');return;}
    const f=report.filters;
    const filterText=[`เทอม ${f.term}`,`ปีการศึกษา ${f.year}`,`สำนักวิชา ${f.faculty}`,`สาขา ${f.program}`,f.query?'ค้นหา: '+f.query:''].filter(Boolean).join(' · ');
    const summary=report.summary.length?`<table class="summary"><tbody>${report.summary.map(([label,value])=>`<tr><th>${esc(label)}</th><td>${value}</td></tr>`).join('')}</tbody></table>`:'';
    const content=`<table><thead><tr>${report.headers.map(h=>`<th>${esc(h)}</th>`).join('')}</tr></thead><tbody>${report.rows.length?report.rows.map(row=>`<tr>${row.map(value=>`<td>${esc(value??'—')}</td>`).join('')}</tr>`).join(''):`<tr><td colspan="${report.headers.length}">ไม่พบข้อมูลตามเงื่อนไข</td></tr>`}</tbody></table>`;
    w.document.open();w.document.write(`<!doctype html><html lang="th"><head><meta charset="utf-8"><title>${esc(report.title)}</title><style>
        @page{size:A4 landscape;margin:15mm}body{font-family:Tahoma,sans-serif;color:#1c1c1e;padding:20px;font-size:12px}h1{text-align:center;font-size:23px;margin-bottom:8px}header{text-align:center;border-bottom:2px solid #7a2222;margin-bottom:20px;padding-bottom:12px}.filters{color:#555;font-size:11px;line-height:1.7}table{border-collapse:collapse;width:100%;margin-top:16px}th,td{border:1px solid #ddd;padding:8px;text-align:left;vertical-align:top}th{background:#f4f4f5}thead{display:table-header-group}tr{break-inside:avoid}.summary{width:55%;margin:15px 0}.summary td{text-align:right}footer{margin-top:16px;font-size:11px;color:#555}.actions{margin-bottom:20px}@media print{body{padding:0}.actions{display:none}}
    </style></head><body><div class="actions"><button id="printReport">พิมพ์ / บันทึก PDF</button></div><header><h1>${esc(report.title)}</h1><p>${esc(report.schoolName)}</p><p class="filters">${esc(filterText)}</p></header>${summary}${content}<footer>ทั้งหมด ${report.rows.length} รายการ · จัดทำเมื่อ ${esc(new Date().toLocaleString('th-TH'))}</footer></body></html>`);w.document.close();
    const ready=()=>{if(w.closed)return;w.document.getElementById('printReport').onclick=()=>w.print();Promise.resolve(w.document.fonts?.ready).then(()=>{if(!w.closed)w.print();});};
    if(w.document.readyState==='complete')ready();else w.addEventListener('load',ready,{once:true});
 }
 let teacherSchoolRequest = 0;
 let teacherSchoolSaving = false;
 function validTeacherSchool(school) {
    return Number.isSafeInteger(school?.schoolId)&&school.schoolId>0&&typeof school.name==='string'&&
        school.schoolId===state.profile.schoolId;
 }
 function applyTeacherSchool(school) {
    state.teacherSchool=school;
    state.profile.school=school;
    const index=schools.findIndex(s=>s.schoolId===school.schoolId);
    if(index<0)schools.push(school);else schools[index]=school;
 }
 async function loadTeacherSchool() {
    if(state.role!=='teacher')return;
    const request=++teacherSchoolRequest,generation=loginGeneration,schoolId=state.profile.schoolId;
    const current=()=>request===teacherSchoolRequest&&generation===loginGeneration&&state.role==='teacher'&&state.profile.schoolId===schoolId;
    state.teacherSchoolLoaded=true;state.teacherSchoolLoading=true;state.teacherSchoolError='';
    if(state.page==='schools')render('schools');
    try {
        const results=await Promise.allSettled([
            window.MfuApi.request('SchoolDetailsController.get'),
            window.MfuApi.request('PrefixController.getAll')
        ]);
        if(!current())return;
        if(results[0].status==='rejected')throw results[0].reason;
        const school=results[0].value;
        if(!validTeacherSchool(school))throw Error('ข้อมูลโรงเรียนไม่ตรงกับบัญชีที่เข้าสู่ระบบ');
        applyTeacherSchool(school);
        state.teacherSchoolRefs={prefixes:results[1].status==='fulfilled'&&Array.isArray(results[1].value)?results[1].value:[]};
        if(school.hasLogo){await refreshSchoolLogo(current);}
        else if(state.schoolLogoUrl){URL.revokeObjectURL(state.schoolLogoUrl);state.schoolLogoUrl=null;}
    }catch(error){if(current())state.teacherSchoolError=error.message||'โหลดข้อมูลโรงเรียนไม่สำเร็จ';}
    finally{if(current()){state.teacherSchoolLoading=false;if(state.page==='schools')render('schools');}}
 }
 async function refreshSchoolLogo(current) {
    try{
        const blob=await window.MfuApi.request('SchoolDetailsController.logo',{responseType:'blob'});
        if(!current())return;
        const url=URL.createObjectURL(blob);
        if(state.schoolLogoUrl)URL.revokeObjectURL(state.schoolLogoUrl);
        state.schoolLogoUrl=url;
    }catch(error){if(current())toast('บันทึกข้อมูลแล้ว แต่โหลดตราโรงเรียนไม่สำเร็จ กรุณาลองเปิดหน้านี้อีกครั้ง');}
 }
 async function saveTeacherSchool(form,data) {
    if(teacherSchoolSaving||state.role!=='teacher'||!state.teacherSchool)return;
    const school=state.teacherSchool,generation=loginGeneration;
    const current=()=>generation===loginGeneration&&state.role==='teacher'&&state.profile.schoolId===school.schoolId;
    const name=String(data.name||'').trim(),schoolType=String(data.schoolType||'').trim(),file=form.elements.schoolLogo?.files[0];
    if(!name||name.length>200||schoolType.length>50){toast('กรุณาตรวจสอบชื่อและประเภทโรงเรียน');return;}
    if(file&&(!['image/png','image/jpeg'].includes(file.type)||file.size>2*1024*1024)){toast('เลือกตราโรงเรียน JPG/PNG ไม่เกิน 2 MB');return;}
    const payload={name,schoolType:schoolType||null,email:String(data.email||'').trim(),phone:String(data.phone||'').trim(),subdistrictId:data.subdistrictId?Number(data.subdistrictId):school.subdistrict?.subdistrictId||null};
    teacherSchoolSaving=true;const button=form.querySelector('button[type="submit"]');if(button){button.disabled=true;button.textContent='กำลังบันทึก…';}
    try{
        let operation='SchoolDetailsController.save',body=payload;
        if(file){operation='SchoolDetailsController.saveWithLogo';body=new FormData();body.append('details',new Blob([JSON.stringify(payload)],{type:'application/json'}));body.append('logo',file);}
        const saved=await window.MfuApi.request(operation,{body});if(!current())return;
        if(!validTeacherSchool(saved))throw Error('ข้อมูลที่บันทึกไม่ถูกต้อง');
        applyTeacherSchool(saved);if(saved.hasLogo)await refreshSchoolLogo(current);if(!current())return;
        if(state.page==='schools')render('schools');toast('บันทึกข้อมูลโรงเรียนแล้ว');
    }catch(error){if(current())toast(error.message||'บันทึกข้อมูลไม่สำเร็จ');}
    finally{if(current()){teacherSchoolSaving=false;if(button?.isConnected){button.disabled=false;button.textContent='ยืนยันข้อมูล';}}}
 }
 async function saveSchoolPrincipal(form,data) {
    if(teacherSchoolSaving||state.role!=='teacher'||!state.teacherSchool)return;
    const generation=loginGeneration,schoolId=state.profile.schoolId;
    const current=()=>generation===loginGeneration&&state.role==='teacher'&&state.profile.schoolId===schoolId;
    teacherSchoolSaving=true;const button=form.querySelector('button[type="submit"]');if(button){button.disabled=true;button.textContent='กำลังบันทึก…';}
    try{
        const payload={prefixId:Number(data.prefixId),firstName:data.firstName,lastName:data.lastName,position:data.position,education:data.education,license:data.license,email:data.email};
        const saved=await window.MfuApi.request('SchoolDetailsController.principal',{body:payload});if(!current())return;
        if(!validTeacherSchool(saved))throw Error('ข้อมูลที่บันทึกไม่ถูกต้อง');applyTeacherSchool(saved);
        if(state.page==='schools')render('schools');toast('บันทึกข้อมูลผู้อำนวยการแล้ว');
    }catch(error){if(current())toast(error.message||'บันทึกข้อมูลไม่สำเร็จ');}
    finally{if(current()){teacherSchoolSaving=false;if(button?.isConnected){button.disabled=false;button.textContent='ยืนยันข้อมูล';}}}
 }
 function backendTeacherSchoolPage() {
    const head=heading('จัดการข้อมูลโรงเรียน');
    if(state.teacherSchoolLoading)return head+panel('<p role="status">กำลังโหลดข้อมูลโรงเรียน…</p>');
    if(state.teacherSchoolError)return head+panel(`<p role="alert">${esc(state.teacherSchoolError)}</p>${btn('ลองอีกครั้ง','teacher-school-retry','btn-primary')}`);
    const school=state.teacherSchool;
    if(!school)return head;
    const province=school.subdistrict?.district?.province?.name||'ยังไม่ได้ระบุจังหวัด';
    const director=state.teacherSchoolTab==='director';
    const summary=panel(`<div class="section-caption"><div><h2>${esc(school.name)}</h2><small>รหัส ${esc(school.schoolCode||'—')} · ${esc(province)}</small></div>${tag('ข้อมูลโรงเรียน')}</div>`);
    const tabs=`<div class="tabs school-tabs"><button type="button" class="tab ${director?'':'active'}" data-ui="school-general">ข้อมูลทั่วไป</button><button type="button" class="tab ${director?'active':''}" data-ui="school-director">ข้อมูลผู้อำนวยการสถานศึกษา</button></div>`;
    if(director){
        const p=school.principal||{},prefixes=state.teacherSchoolRefs?.prefixes||[];
        return head+summary+tabs+`<div id="schoolEditor"><form id="directorForm" class="school-stack">
            <label>คำนำหน้า *<select name="prefixId" required><option value="">เลือกคำนำหน้า</option>${prefixes.map(prefix=>`<option value="${prefix.prefixId}" ${prefix.prefixId===p.prefixId?'selected':''}>${esc(prefix.prefixName)}</option>`).join('')}</select></label>
            ${field('ชื่อ','firstName',p.firstName||'','text',true,'maxlength="100"')}
            ${field('นามสกุล','lastName',p.lastName||'','text',true,'maxlength="100"')}
            ${field('ตำแหน่ง','position',p.position||'','text',true,'maxlength="100"')}
            ${field('วุฒิการศึกษาสูงสุด','education',p.education||'','text',false,'maxlength="200"')}
            ${field('เลขที่ใบอนุญาตประกอบวิชาชีพ','license',p.license||'','text',false,'maxlength="50"')}
            ${field('อีเมลติดต่อ','email',p.email||'','email',true,'maxlength="150"')}
            <div class="form-actions">${btn('ยกเลิก','teacher-school-cancel','btn-outline')}<button type="submit" class="btn btn-primary">ยืนยันข้อมูล</button></div>
        </form></div>`;
    }
    return head+summary+tabs+`<div id="schoolEditor"><form id="schoolForm" class="school-stack">
        ${field('ชื่อโรงเรียน','name',school.name,'text',true,'maxlength="200"')}
        ${field('รหัสโรงเรียน','schoolCode',school.schoolCode||'','text',false,'readonly')}
        ${field('สังกัด / ประเภทโรงเรียน','schoolType',school.schoolType||'','text',false,'maxlength="50"')}
        ${field('จังหวัด','province',province,'text',false,'readonly')}
        ${field('อีเมลโรงเรียน','email',school.email||'','email',true,'maxlength="150"')}
        ${field('โทรศัพท์โรงเรียน','phone',school.phone||'','tel',true,'maxlength="20"')}
        ${field('ตราโรงเรียน · JPG/PNG ไม่เกิน 2 MB','schoolLogo','','file',false,'accept="image/png,image/jpeg"')}
        ${state.schoolLogoUrl?`<img src="${esc(state.schoolLogoUrl)}" alt="ตราโรงเรียน" style="max-width:120px;max-height:120px;object-fit:contain">`:''}
        <div class="form-actions">${btn('ยกเลิก','teacher-school-cancel','btn-outline')}<button type="submit" class="btn btn-primary">ยืนยันข้อมูล</button></div>
    </form></div>`;
 }
 let liveAdminReportRequest = 0;
 async function loadLiveAdminReports() {
    if(state.role!=='admin')return;
    const request=++liveAdminReportRequest,generation=loginGeneration;
    const current=()=>request===liveAdminReportRequest&&generation===loginGeneration&&state.role==='admin';
    state.liveAdminReportsLoaded=true;state.liveAdminReportsLoading=true;state.liveAdminReportsError='';
    if(['analysis','reports'].includes(state.page))render(state.page);
    try {
        const operations=['ProgramInterestController.getAll','ApplicantController.getAll','ApplicantSelectionController.getAll','SchoolController.getAllSchools'];
        const responses=await Promise.allSettled(operations.map(op=>window.MfuApi.request(op)));
        if(!current())return;
        const values=responses.map((result,index)=>{
            if(result.status==='rejected')throw result.reason;
            if(!Array.isArray(result.value))throw Error('รูปแบบข้อมูลรายงานไม่ถูกต้อง: '+operations[index]);
            return result.value;
        });
        const [interestRows,applicantRows,selectionRows,schoolRows]=values;
        if(!interestRows.every(i=>i?.program?.programId&&i.applicant?.applicantId)||
           !applicantRows.every(a=>a?.applicantId)||!selectionRows.every(r=>r?.entryGroup&&r.applicant?.applicantId))throw Error('ข้อมูลสำหรับรายงานไม่ครบถ้วน');
        state.liveAdminReportsData={interests:interestRows,applicants:applicantRows,selections:selectionRows,schools:schoolRows};
    }catch(error){if(current())state.liveAdminReportsError=error.message||'โหลดข้อมูลรายงานไม่สำเร็จ';}
    finally{if(current()){state.liveAdminReportsLoading=false;if(['analysis','reports'].includes(state.page))render(state.page);}}
 }
 function liveReportSnapshot(page) {
    const data=state.liveAdminReportsData;
    if(!data)return null;
    const f=state.liveAdminReportFilters?.[page]||{};
    const now=new Date(),cutoff=new Date(now);cutoff.setDate(cutoff.getDate()-30);
    const schoolMap=new Map(data.schools.map(s=>[s.schoolId,s]));
    const schoolFor=a=>schoolMap.get(a.school?.schoolId)||a.school;
    const regionMatches=a=>!f.region||schoolFor(a)?.subdistrict?.district?.province?.region===f.region;
    const roundMatches=g=>!f.round||g?.entrygroupName===f.round;
    const inPeriod=date=>{
        if(page!=='analysis'||f.period!=='30')return true;
        if(!date)return false;
        // Backend LocalDateTime has no offset; compare as server-style wall-clock dates.
        const timestamp=new Date(date);return !Number.isNaN(timestamp.getTime())&&timestamp>=cutoff&&timestamp<=now;
    };
    const interests=data.interests.filter(i=>regionMatches(i.applicant)&&roundMatches(i.program.entryGroup)&&inPeriod(i.interestedAt));
    const selectionRows=data.selections.filter(r=>regionMatches(r.applicant)&&roundMatches(r.entryGroup));
    const selectedIds=new Set(selectionRows.map(r=>r.applicant.applicantId));
    const applicantRows=data.applicants.filter(a=>regionMatches(a)&&(!f.round||selectedIds.has(a.applicantId)));
    const rankingMap=new Map();
    for(const i of interests){
        const p=i.program;
        if(!rankingMap.has(p.programId))rankingMap.set(p.programId,{programId:p.programId,name:p.programName,faculty:p.faculty||'ไม่ระบุสำนักวิชา',count:0});
        rankingMap.get(p.programId).count++;
    }
    const rankings=[...rankingMap.values()].sort((a,b)=>b.count-a.count||a.programId-b.programId).map((r,index)=>({...r,rank:index+1,percent:interests.length?r.count*100/interests.length:0}));
    const facultyMap=new Map();for(const r of rankings)facultyMap.set(r.faculty,(facultyMap.get(r.faculty)||0)+r.count);
    const faculties=[...facultyMap.entries()].map(([name,count])=>({name,count})).sort((a,b)=>b.count-a.count||a.name.localeCompare(b.name));
    const schoolCounts=new Map();for(const a of applicantRows){const school=schoolFor(a);if(!school?.schoolId)continue;if(!schoolCounts.has(school.schoolId))schoolCounts.set(school.schoolId,{id:school.schoolId,name:school.name,count:0});schoolCounts.get(school.schoolId).count++;}
    const bySchool=[...schoolCounts.values()].sort((a,b)=>b.count-a.count||a.id-b.id);
    const roundCounts=new Map();
    for(const r of selectionRows){if(!r.publishedAt||!inPeriod(r.publishedAt))continue;const name=r.entryGroup.entrygroupName;if(name)roundCounts.set(name,(roundCounts.get(name)||0)+1);}
    const roundTotal=[...roundCounts.values()].reduce((sum,n)=>sum+n,0);
    const rounds=[...roundCounts.entries()].map(([name,count])=>({name,count,percent:roundTotal?count*100/roundTotal:0})).sort((a,b)=>a.name.localeCompare(b.name));
    return {page,title:page==='analysis'?'วิเคราะห์ความสนใจหลักสูตร':'รายงานภาพรวมระดับระบบ',filters:{...f},generatedAt:now.toISOString(),rankings,faculties,bySchool,rounds,totalInterests:interests.length,roundTotal};
 }
 function liveReportFilterDescription(snapshot) {
    const f=snapshot.filters;
    return [f.round||'ทุกรอบ',f.region||'ทุกภูมิภาค',snapshot.page==='analysis'?(f.period==='30'?'30 วันล่าสุด':'ทุกช่วงเวลา'):'ทุกช่วงเวลา'].join(' · ');
 }
 function staticReportChart(html) {
    return html.replace(/ data-ui="[^"]*"/g,'').replace(/<button\b/g,'<div').replace(/<\/button>/g,'</div>');
 }
 function liveAdminReportBody(snapshot) {
    const empty=title=>panel(`<h3>${esc(title)}</h3><div class="empty-state">ไม่พบข้อมูลตามเงื่อนไข</div>`);
    const roundChart=snapshot.rounds.length?staticReportChart(chart('สัดส่วนรอบการรับสมัคร',snapshot.rounds.map(r=>r.name),snapshot.rounds.map(r=>r.count),'pie','round')):empty('สัดส่วนรอบการรับสมัคร');
    if(snapshot.page==='analysis'){
        const max=Math.max(1,...snapshot.rankings.map(r=>r.count));
        const ranking=`<div class="panel admin-ranking"><div class="admin-panel-heading"><h3>อันดับความสนใจหลักสูตร</h3><small>จัดอันดับตามจำนวนรายการความสนใจ</small></div>${snapshot.rankings.length?snapshot.rankings.map(r=>`<div class="rank-row"><span class="rank-number">${r.rank}</span><span><strong>${esc(r.name)}</strong><i style="width:${r.count/max*100}%"></i></span><b>${r.count}</b></div>`).join(''):'<div class="empty-state">ไม่พบข้อมูลตามเงื่อนไข</div>'}</div>`;
        const faculty=snapshot.faculties.length?staticReportChart(groupedChart('จำนวนความสนใจรวมตามสำนักวิชา',snapshot.faculties.map(r=>r.name),['จำนวนความสนใจ'],snapshot.faculties.map(r=>[r.count]),'faculty')):empty('จำนวนความสนใจรวมตามสำนักวิชา');
        return `<div class="admin-analysis-grid">${ranking}<div class="admin-analysis-aside">${faculty}${roundChart}</div></div>`;
    }
    const ranking=`<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>อันดับความสนใจหลักสูตร</h3></div>${snapshot.rankings.length?table(['อันดับ','หลักสูตร','สำนักวิชา','จำนวนผู้สนใจ','สัดส่วน'],snapshot.rankings.map(r=>[r.rank,esc(r.name),esc(r.faculty),r.count,r.percent.toFixed(1)+'%'])):'<div class="empty-state">ไม่พบข้อมูลตามเงื่อนไข</div>'}</div>`;
    const school=snapshot.bySchool.length?staticReportChart(chart('จำนวนผู้สมัครตามโรงเรียน',snapshot.bySchool.map(r=>r.name),snapshot.bySchool.map(r=>r.count),'bar','school')):empty('จำนวนผู้สมัครตามโรงเรียน');
    return ranking+`<div class="admin-report-bottom">${school}${roundChart}</div>`;
 }
 function liveAdminReportPage(page) {
    const title=page==='analysis'?'วิเคราะห์ความสนใจหลักสูตร':'รายงานภาพรวมระดับระบบ';
    const head=heading(title,page==='reports'?'ภาพรวมความสนใจหลักสูตรและข้อมูลผู้สมัครจากทุกโรงเรียน':'');
    if(state.liveAdminReportsLoading)return head+panel('<p role="status">กำลังโหลดข้อมูลรายงาน…</p>');
    if(state.liveAdminReportsError)return head+panel(`<p role="alert">${esc(state.liveAdminReportsError)}</p>${btn('ลองอีกครั้ง','live-admin-report-retry','btn-primary')}`);
    const data=state.liveAdminReportsData;
    if(!data)return head;
    const f=state.liveAdminReportFilters?.[page]||{};
    const rounds=[...new Set([...data.interests.map(i=>i.program.entryGroup?.entrygroupName),...data.selections.map(r=>r.entryGroup.entrygroupName)].filter(Boolean))].sort();
    const regions=[...new Set(data.schools.map(s=>s.subdistrict?.district?.province?.region).filter(Boolean))].sort();
    const selectFilter=(label,name,items,all)=>`<label>${esc(label)}<select name="${name}"><option value="">${all}</option>${items.map(item=>`<option value="${esc(item)}" ${item===f[name]?'selected':''}>${esc(item)}</option>`).join('')}</select></label>`;
    const filters=`<form id="adminReportFilter" class="filter-panel admin-filter">
        ${selectFilter('รอบการรับสมัคร','round',rounds,'ทุกรอบ')}
        ${page==='analysis'?`<label>ช่วงเวลา<select name="period"><option value="">ทั้งหมด</option><option value="30" ${f.period==='30'?'selected':''}>30 วันล่าสุด</option></select></label>`:''}
        ${selectFilter('ภูมิภาค','region',regions,'ทุกภูมิภาค')}
        ${page==='reports'?'<button class="btn btn-primary">แสดงรายงาน</button>':''}
        ${btn('ดาวน์โหลดรายงาน (CSV/PDF)','admin-export-options',page==='analysis'?'btn-primary':'btn-outline')}
    </form>`;
    return head+filters+`<div id="liveAdminReportData">${liveAdminReportBody(liveReportSnapshot(page))}</div>`;
 }
 function liveReportCsv(snapshot) {
    const cell=value=>{
        let text=String(value??'');if(/^[\s]*[=+@\-]/.test(text))text="'"+text;
        return '"'+text.replace(/"/g,'""')+'"';
    };
    const lines=[];const add=(...values)=>lines.push(values.map(cell).join(','));
    add(snapshot.title);add('ตัวกรอง',liveReportFilterDescription(snapshot));add('สร้างเมื่อ',snapshot.generatedAt);add();
    add('อันดับความสนใจหลักสูตร');add('อันดับ','รหัสหลักสูตร','หลักสูตร','สำนักวิชา','จำนวนรายการความสนใจ','สัดส่วน (%)');
    for(const r of snapshot.rankings)add(r.rank,r.programId,r.name,r.faculty,r.count,r.percent.toFixed(2));
    add();
    if(snapshot.page==='analysis'){
        add('จำนวนความสนใจรวมตามสำนักวิชา');add('สำนักวิชา','จำนวนรายการความสนใจ');for(const r of snapshot.faculties)add(r.name,r.count);
    }else{
        add('จำนวนผู้สมัครตามโรงเรียน');add('รหัสโรงเรียน','โรงเรียน','จำนวนผู้สมัคร');for(const r of snapshot.bySchool)add(r.id,r.name,r.count);
    }
    add();add('สัดส่วนรอบการรับสมัคร (รายการผลที่เผยแพร่)');add('รอบการรับสมัคร','จำนวนรายการ','สัดส่วน (%)');for(const r of snapshot.rounds)add(r.name,r.count,r.percent.toFixed(2));
    return '\uFEFF'+lines.join('\r\n');
 }
 function exportLiveAdminReport(format) {
    if(!['analysis','reports'].includes(state.page)||!state.liveAdminReportsData||state.liveAdminReportsError||state.liveAdminReportsLoading)return;
    const snapshot=liveReportSnapshot(state.page);
    get('uiDialog').close();
    if(format==='csv'){
        const blob=new Blob([liveReportCsv(snapshot)],{type:'text/csv;charset=utf-8;'}),url=URL.createObjectURL(blob),a=document.createElement('a');
        a.href=url;a.download=(snapshot.page==='analysis'?'program-interest-analysis':'system-overview-report')+'.csv';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);return;
    }
    const printWindow=createReportPrintFrame();
    if(!printWindow){toast('ไม่สามารถเตรียมเอกสาร PDF ได้');return;}
    const sections=[{title:'อันดับความสนใจหลักสูตร',headers:['อันดับ','หลักสูตร','สำนักวิชา','จำนวน','สัดส่วน'],rows:snapshot.rankings.map(r=>[r.rank,r.name,r.faculty,r.count,r.percent.toFixed(1)+'%'])},
        snapshot.page==='analysis'?{title:'จำนวนความสนใจรวมตามสำนักวิชา',headers:['สำนักวิชา','จำนวน'],rows:snapshot.faculties.map(r=>[r.name,r.count])}:{title:'จำนวนผู้สมัครตามโรงเรียน',headers:['โรงเรียน','จำนวน'],rows:snapshot.bySchool.map(r=>[r.name,r.count])},
        {title:'สัดส่วนรอบการรับสมัคร (รายการผลที่เผยแพร่)',headers:['รอบ','จำนวน','สัดส่วน'],rows:snapshot.rounds.map(r=>[r.name,r.count,r.percent.toFixed(1)+'%'])}];
    const content=sections.map(section=>`<section><h2>${esc(section.title)}</h2><table><thead><tr>${section.headers.map(h=>`<th>${esc(h)}</th>`).join('')}</tr></thead><tbody>${section.rows.length?section.rows.map(row=>`<tr>${row.map(value=>`<td>${esc(value)}</td>`).join('')}</tr>`).join(''):`<tr><td colspan="${section.headers.length}">ไม่พบข้อมูลตามเงื่อนไข</td></tr>`}</tbody></table></section>`).join('');
    const stylesheet=new URL('/styles.css',window.location.origin).href;
    printWindow.document.open();
    printWindow.document.write(`<!doctype html><html lang="th"><head><meta charset="utf-8"><title>${esc(snapshot.title)}</title><link rel="stylesheet" href="${esc(stylesheet)}"><style>
        @page{size:A4 landscape;margin:14mm}body{background:white;color:#1c1c1e;padding:24px;font-family:Tahoma,sans-serif}h1{font-size:24px;color:#7a2222}h2{font-size:17px;margin-top:24px}table{border-collapse:collapse;width:100%;font-size:12px}th,td{text-align:left;border-bottom:1px solid #ddd;padding:8px}th{background:#f4f4f5}thead{display:table-header-group}tr{break-inside:avoid}section{margin-bottom:20px}p{font-size:12px}button{padding:10px 18px}@media print{body{padding:0}.print-controls{display:none}}
    </style></head><body><div class="print-controls"><button id="savePdf">พิมพ์ / บันทึกเป็น PDF</button></div><h1>${esc(snapshot.title)}</h1><p>${esc(liveReportFilterDescription(snapshot))}</p>${content}</body></html>`);
    printWindow.document.close();
    const prepare=()=>{if(printWindow.closed)return;printWindow.document.getElementById('savePdf').onclick=()=>printWindow.print();Promise.resolve(printWindow.document.fonts?.ready).then(()=>{if(!printWindow.closed)printWindow.print();});};
    if(printWindow.document.readyState==='complete')prepare();else printWindow.addEventListener('load',prepare,{once:true});
 }
 function openResultGroup(roundId, quotaId) {
    const row = (state.adminResultsRows||[]).find(r => r.entryGroup.entrygroupId===roundId && r.quota.quotaId===quotaId);
    if (!row) return;
    state.adminResultGroupPage = state.adminResultsPage||0;
    state.adminResultsView = 'applicants';
    state.adminResultFilters = {
        year:String(row.entryGroup.academicYear),round:String(roundId),quota:String(quotaId),
        status:'',nationalId:'',name:'',school:''
    };
    state.adminResultsPage = 0;
    render('results');
 }
 function showResultApplicant(id) {
    const r = (state.adminResultsRows||[]).find(r=>r.applicationId===id);
    if (!r) return;
    const a=r.applicant;
    openModal('รายละเอียดผู้สมัคร',`<dl>
        <dt>ชื่อ–นามสกุล</dt><dd>${esc([a.firstName,a.lastName].filter(Boolean).join(' '))}</dd>
        <dt>เลขบัตรประชาชน</dt><dd>${esc(a.nationalId)}</dd>
        <dt>โรงเรียน</dt><dd>${esc(a.school?.name||'—')}</dd>
        <dt>ปีการศึกษา</dt><dd>${esc(r.entryGroup.academicYear)}</dd>
        <dt>รอบการรับสมัคร</dt><dd>${esc(r.entryGroup.entrygroupName)}</dd>
        <dt>โครงการ / โควตา</dt><dd>${esc(r.quota.quotaName)}</dd>
        <dt>สถานะ</dt><dd>${esc(r.status.statusCode)} ${esc(r.status.description)}</dd>
        <dt>วันที่เผยแพร่</dt><dd>${esc(r.publishedAt?.replace('T',' ')||'ยังไม่เผยแพร่')}</dd>
    </dl>`);
 }
 function groupedAdminResults() {
    const f=state.adminResultGroupFilters||{}, groups=new Map();
    for (const r of state.adminResultsRows||[]) {
        if (f.year && String(r.entryGroup.academicYear)!==f.year) continue;
        if (f.round && String(r.entryGroup.entrygroupId)!==f.round) continue;
        if (f.quota && String(r.quota.quotaId)!==f.quota) continue;
        const key=r.entryGroup.entrygroupId+':'+r.quota.quotaId;
        if (!groups.has(key)) groups.set(key,{round:r.entryGroup,quota:r.quota,total:0,published:0});
        const g=groups.get(key);g.total++;if(r.publishedAt)g.published++;
    }
    return [...groups.values()].map(g=>({...g,publication:g.published===g.total?'PUBLISHED':g.published===0?'UNPUBLISHED':'MIXED'}))
        .filter(g=>!f.publication||g.publication===f.publication)
        .sort((a,b)=>(b.round.academicYear-a.round.academicYear)||(a.round.entrygroupId-b.round.entrygroupId)||(a.quota.quotaId-b.quota.quotaId));
 }
 function backendAdminResultsPage() {
    if (state.adminResultsView==='applicants') return backendAdminApplicantsPage();
    const head=heading('ผลการรับสมัคร');
    if(state.adminResultsLoading)return head+panel('<p role="status">กำลังโหลดผลการรับสมัคร…</p>');
    if(state.adminResultsError)return head+panel(`<p role="alert">${esc(state.adminResultsError)}</p>${btn('ลองอีกครั้ง','admin-results-retry','btn-primary')}`);
    const source=state.adminResultsRows||[],f=state.adminResultGroupFilters||{};
    const unique=(items,key)=>[...new Map(items.map(i=>[String(i[key]),i])).values()];
    const rounds=unique(source.map(r=>r.entryGroup),'entrygroupId');
    const quotas=unique(source.map(r=>r.quota),'quotaId');
    const selectOptions=(label,name,items,key,labelKey)=>`<label>${esc(label)}<select name="${name}"><option value="">ทั้งหมด</option>${items.map(i=>`<option value="${esc(i[key])}" ${String(i[key])===String(f[name]||'')?'selected':''}>${esc(i[labelKey])}</option>`).join('')}</select></label>`;
    const filters=`<form id="adminResultFilter" class="filter-panel admin-filter">
        ${field('ปีการศึกษา','year',f.year||'','text',false,'inputmode="numeric" pattern="[0-9]{4}" maxlength="4" placeholder="ทุกปีการศึกษา"')}
        ${selectOptions('รอบการรับสมัคร','round',rounds,'entrygroupId','entrygroupName')}
        ${selectOptions('โครงการ / โควตา','quota',quotas,'quotaId','quotaName')}
        ${selectOptions('สถานะการเผยแพร่','publication',[{id:'PUBLISHED',name:'เผยแพร่แล้ว'},{id:'UNPUBLISHED',name:'ยังไม่เผยแพร่'},{id:'MIXED',name:'เผยแพร่บางส่วน'}],'id','name')}
        <button class="btn btn-primary">ค้นหา</button>
    </form>`;
    const matches=groupedAdminResults(),pages=Math.ceil(matches.length/10);
    const page=Math.max(0,Math.min(Number(state.adminResultsPage)||0,Math.max(0,pages-1)));
    state.adminResultsPage=page;
    const names={PUBLISHED:'เผยแพร่แล้ว',UNPUBLISHED:'ยังไม่เผยแพร่',MIXED:'เผยแพร่บางส่วน'};
    const rows=matches.slice(page*10,page*10+10);
    const body=rows.length?table(['โครงการ / โควตา','ปีการศึกษา','รอบการรับสมัคร','จำนวนรายการ','สถานะ','การดำเนินการ'],rows.map(g=>[
        esc(g.quota.quotaName),esc(g.round.academicYear),esc(g.round.entrygroupName),g.total,
        esc(names[g.publication]),btn('ดูรายชื่อ','admin-result-open','text-button',`data-round="${g.round.entrygroupId}" data-quota="${g.quota.quotaId}"`)
    ])):'<div class="empty-state">ไม่พบผลการรับสมัครตามเงื่อนไข</div>';
    return head+filters+`<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>กลุ่มผลการคัดเลือก</h3><small>${matches.length} กลุ่ม</small></div><div style="overflow-x:auto">${body}</div>
    <div class="form-actions">${btn('ก่อนหน้า','admin-results-page','btn-outline',`data-page="${page-1}" ${page===0?'disabled':''}`)}<span>หน้า ${pages?page+1:0} / ${pages}</span>${btn('ถัดไป','admin-results-page','btn-outline',`data-page="${page+1}" ${page+1>=pages?'disabled':''}`)}</div></div>`;
 }
 let adminResultsRequest = 0;
 async function loadAdminResults() {
    if (state.role !== 'admin') return;
    const request = ++adminResultsRequest, generation = loginGeneration;
    const current = () => request === adminResultsRequest && generation === loginGeneration && state.role === 'admin';
    state.adminResultsLoaded = true;
    state.adminResultsLoading = true;
    state.adminResultsError = '';
    if (state.page === 'results') render('results');
    try {
        const rows = await window.MfuApi.request('ApplicantSelectionController.getAll');
        if (!current()) return;
        if (!Array.isArray(rows) || !rows.every(r => Number.isSafeInteger(r.applicationId) &&
            r.applicant && r.entryGroup && r.quota && r.status)) {
            throw new Error('รูปแบบข้อมูลผลการรับสมัครจาก Backend ไม่ถูกต้อง');
        }
        state.adminResultsRows = [...rows].sort((a,b) => a.applicationId-b.applicationId);
        state.adminResultsPage = 0;
    } catch (error) {
        if (current()) state.adminResultsError = error.message || 'โหลดผลการรับสมัครไม่สำเร็จ';
    } finally {
        if (current()) {
            state.adminResultsLoading = false;
            if (state.page === 'results') render('results');
        }
    }
 }
 function filteredAdminResults() {
    const f = state.adminResultFilters || {};
    const contains = (value,query) => String(value||'').toLocaleLowerCase().includes(String(query||'').trim().toLocaleLowerCase());
    return (state.adminResultsRows || []).filter(r => {
        const a=r.applicant, g=r.entryGroup;
        return (!f.year || String(g.academicYear)===f.year) &&
            (!f.round || String(g.entrygroupId)===f.round) &&
            (!f.quota || String(r.quota.quotaId)===f.quota) &&
            (!f.status || String(r.status.statusCode)===f.status) &&
            contains(a.nationalId,f.nationalId) &&
            contains([a.firstName,a.lastName].filter(Boolean).join(' '),f.name) &&
            contains(a.school?.name,f.school);
    });
 }
 function backendAdminApplicantsPage() {
    const head = heading('รายชื่อผู้สมัคร')+btn('← กลับไปหน้าผลการรับสมัคร','admin-result-back','text-button');
    if (state.adminResultsLoading) return head+panel('<p role="status">กำลังโหลดผลการรับสมัคร…</p>');
    if (state.adminResultsError) return head+panel(`<p role="alert">${esc(state.adminResultsError)}</p>${btn('ลองอีกครั้ง','admin-results-retry','btn-primary')}`);
    const source = state.adminResultsRows || [], f = state.adminResultFilters || {};
    const unique = (items,key) => [...new Map(items.map(i => [String(i[key]),i])).values()];
    const years = [...new Set(source.map(r => r.entryGroup.academicYear).filter(v=>v!=null))].sort((a,b)=>b-a);
    const rounds = unique(source.filter(r=>!f.year||String(r.entryGroup.academicYear)===f.year).map(r=>r.entryGroup),'entrygroupId');
    const quotas = unique(source.map(r=>r.quota),'quotaId');
    const statuses = unique(source.map(r=>r.status),'statusCode').sort((a,b)=>String(a.statusCode).localeCompare(String(b.statusCode)));
    const options = (label,name,items,valueKey,labelKey) => `<label>${esc(label)}<select name="${name}"><option value="">ทั้งหมด</option>${items.map(i => `<option value="${esc(i[valueKey])}" ${String(i[valueKey])===String(f[name]||'')?'selected':''}>${esc(i[labelKey])}</option>`).join('')}</select></label>`;
    const filters = `<form id="adminResultFilter" class="filter-panel admin-filter result-applicant-filters">
        ${field('ปีการศึกษา','year',f.year||'','text',false,'inputmode="numeric" pattern="[0-9]{4}" maxlength="4" placeholder="ทุกปีการศึกษา"')}
        ${options('รอบการรับสมัคร','round',rounds,'entrygroupId','entrygroupName')}
        ${options('โครงการ / โควตา','quota',quotas,'quotaId','quotaName')}
        ${options('สถานะ','status',statuses,'statusCode','description')}
        ${field('เลขบัตรประชาชน','nationalId',f.nationalId||'','search',false,'maxlength="30"')}
        ${field('ชื่อ–นามสกุล','name',f.name||'','search')}
        ${field('ชื่อโรงเรียน','school',f.school||'','search')}
        <button class="btn btn-primary">ค้นหา</button>
    </form>`;
    const matches = filteredAdminResults();
    const pages = Math.ceil(matches.length/10);
    const page = Math.max(0,Math.min(Number(state.adminResultsPage)||0,Math.max(0,pages-1)));
    state.adminResultsPage = page;
    const rows = matches.slice(page*10,page*10+10);
    const body = rows.length ? table(['เลขบัตรประชาชน','ชื่อ–นามสกุล','โรงเรียน','ปีการศึกษา','รอบ','โครงการ / โควตา','สถานะ'],rows.map(r=>[
        esc(r.applicant.nationalId),esc([r.applicant.firstName,r.applicant.lastName].filter(Boolean).join(' ')),
        esc(r.applicant.school?.name||'—'),esc(r.entryGroup.academicYear),esc(r.entryGroup.entrygroupName),
        esc(r.quota.quotaName),esc(r.status.description||r.status.statusCode)
    ])) : '<div class="empty-state">ไม่พบผลการรับสมัครตามเงื่อนไข</div>';
    const layout = `<style>
        #adminResultFilter.result-applicant-filters {
            display:grid; grid-template-columns:minmax(100px,.65fr) repeat(3,minmax(0,1.25fr)) 140px;
            gap:16px; align-items:end;
        }
        #adminResultFilter.result-applicant-filters > label { min-width:0; width:auto; }
        #adminResultFilter.result-applicant-filters input,
        #adminResultFilter.result-applicant-filters select { width:100%; min-width:0; box-sizing:border-box; }
        #adminResultFilter.result-applicant-filters > button { grid-column:5; grid-row:1; width:100%; }
        #adminResultFilter.result-applicant-filters > label:nth-of-type(5) { grid-column:1 / 3; }
        #adminResultFilter.result-applicant-filters > label:nth-of-type(6) { grid-column:3; }
        #adminResultFilter.result-applicant-filters > label:nth-of-type(7) { grid-column:4 / 6; }
        @media(max-width:1000px) {
            #adminResultFilter.result-applicant-filters { grid-template-columns:repeat(2,minmax(0,1fr)); }
            #adminResultFilter.result-applicant-filters > label:nth-of-type(n) { grid-column:auto; }
            #adminResultFilter.result-applicant-filters > button { grid-column:2; grid-row:auto; }
        }
        @media(max-width:560px) {
            #adminResultFilter.result-applicant-filters { grid-template-columns:minmax(0,1fr); }
            #adminResultFilter.result-applicant-filters > button { grid-column:1; }
        }
    </style>`;
    return layout+head+filters+`<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>รายการผลการรับสมัคร</h3><small>${matches.length} รายการ</small></div>
        <div style="overflow-x:auto">${body}</div>
        <div class="form-actions">
            ${btn('ก่อนหน้า','admin-results-page','btn-outline',`data-page="${page-1}" ${page===0?'disabled':''}`)}
            <span>หน้า ${pages?page+1:0} / ${pages}</span>
            ${btn('ถัดไป','admin-results-page','btn-outline',`data-page="${page+1}" ${page+1>=pages?'disabled':''}`)}
        </div></div>`;
 }
 let adminDashboardRequest = 0;
 function validAdminDashboard(r) {
    const count = n => Number.isSafeInteger(n) && n >= 0;
    const sections = {
        announcements:['total','published','drafts','hidden','expired'],
        applicants:['total'], selectionResults:['total','published','unpublished'],
        resultDatasets:['total','published','pendingReview','erroneous'],
        pendingTasks:['total','pendingReviewDatasets','draftAnnouncements']
    };
    const countsMap = map => map && typeof map === 'object' && !Array.isArray(map) && Object.values(map).every(count);
    return !!r && Object.entries(sections).every(([key,fields]) => r[key] && fields.every(f => count(r[key][f]))) &&
        countsMap(r.announcements.contentByType) && countsMap(r.selectionResults.byStatus) &&
        Array.isArray(r.pendingTasks.items) && r.pendingTasks.items.every(i => i && typeof i.title === 'string' && typeof i.taskType === 'string') &&
        Array.isArray(r.recentActivities) && r.recentActivities.every(i => i && typeof i.actionType === 'string') &&
        typeof r.periodStart === 'string' && typeof r.periodEnd === 'string';
 }
 async function loadAdminDashboard() {
    if (state.role !== 'admin') return;
    const request = ++adminDashboardRequest, generation = loginGeneration;
    const current = () => request === adminDashboardRequest && generation === loginGeneration && state.role === 'admin';
    state.adminDashboardLoaded = true;
    state.adminDashboardLoading = true;
    state.adminDashboardError = '';
    if (state.page === 'admin-dashboard') render('admin-dashboard');
    try {
        const f = state.dashboardFilters || {};
        const query = {days: Number(f.days) || 30};
        if (f.academicYear) query.academicYear = Number(f.academicYear);
        const report = await window.MfuApi.request('AdminDashboardController.getDashboard', {query});
        if (!current()) return;
        if (!validAdminDashboard(report)) throw new Error('รูปแบบข้อมูลแดชบอร์ดจาก Backend ไม่ถูกต้อง');
        state.adminDashboardReport = report;
    } catch (error) {
        if (!current()) return;
        state.adminDashboardError = error.message || 'โหลดแดชบอร์ดไม่สำเร็จ';
    } finally {
        if (current()) {
            state.adminDashboardLoading = false;
            if (state.page === 'admin-dashboard') render('admin-dashboard');
        }
    }
 }
 function dashboardCountCards(items, scope) {
    return '<div class="kpi-grid">'+items.map(([label,count,note]) => `<div class="panel kpi"><span>${esc(label)}</span><strong>${count}</strong><small>${esc(note || scope)}</small></div>`).join('')+'</div>';
 }
 function backendAdminDashboardPage() {
    const f = state.dashboardFilters || {};
    const filters = `<form id="adminDashboardFilter" class="filter-panel admin-filter">
        ${field('ปีการศึกษา (ว่าง = ทุกปี)','academicYear',f.academicYear||'','text',false,'inputmode="numeric" pattern="[0-9]{4}" maxlength="4" placeholder="เช่น 2569"')}
        <label>ช่วงเวลาย้อนหลัง<select name="days">${[7,30,90,365].map(d => `<option value="${d}" ${d===(Number(f.days)||30)?'selected':''}>${d} วันล่าสุด</option>`).join('')}</select></label>
        <button class="btn btn-primary" ${state.adminDashboardLoading?'disabled':''}>ใช้ตัวกรอง</button>
    </form>`;
    const layout = `<style>
        #extendedPage[data-view="admin-dashboard"] .kpi-grid {
            display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px; margin-bottom:24px;
        }
        #extendedPage[data-view="admin-dashboard"] .kpi { min-width:0; }
        #extendedPage[data-view="admin-dashboard"] .admin-dashboard-grid {
            display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:20px; align-items:start;
        }
        #extendedPage[data-view="admin-dashboard"] .admin-dashboard-grid > .panel {
            min-width:0; height:auto; min-height:0; align-self:start;
        }
        #extendedPage[data-view="admin-dashboard"] .admin-dashboard-grid .bars { min-height:0; height:auto; }
        #extendedPage[data-view="admin-dashboard"] .filter-panel { flex-wrap:wrap; }
        @media(max-width:1100px) {
            #extendedPage[data-view="admin-dashboard"] .kpi-grid { grid-template-columns:repeat(2,minmax(0,1fr)); }
        }
        @media(max-width:800px) {
            #extendedPage[data-view="admin-dashboard"] .admin-dashboard-grid { grid-template-columns:minmax(0,1fr); }
        }
        @media(max-width:520px) {
            #extendedPage[data-view="admin-dashboard"] .kpi-grid { grid-template-columns:minmax(0,1fr); }
        }
    </style>`;
    const head = layout+heading('สถานะงานเนื้อหาและผลการรับสมัคร')+filters;
    if (state.adminDashboardLoading) return head+panel('<p role="status">กำลังโหลดแดชบอร์ด…</p>');
    if (state.adminDashboardError) return head+panel(`<p role="alert">${esc(state.adminDashboardError)}</p>${btn('ลองอีกครั้ง','admin-dashboard-retry','btn-primary')}`);
    const r = state.adminDashboardReport;
    if (!r) return head;
    const scope = r.academicYear == null ? 'ทุกปีการศึกษา' : 'ปีการศึกษา '+r.academicYear;
    const range = `${r.periodStart.slice(0,16).replace('T',' ')} ถึง ${r.periodEnd.slice(0,16).replace('T',' ')}`;
    const a = r.announcements, t = r.selectionResults, d = r.resultDatasets;
    const mapChart = (title,map) => {
        const entries = Object.entries(map).sort((a,b) => b[1]-a[1] || a[0].localeCompare(b[0]));
        return entries.length ? bars(title,entries.map(e=>e[0]),entries.map(e=>e[1])) : panel(`<h3>${esc(title)}</h3><p>ไม่มีข้อมูลตามเงื่อนไข</p>`);
    };
    const pending = r.pendingTasks.items.length ? table(['งาน','ประเภท','สถานะ','รายละเอียด'],r.pendingTasks.items.map(i => [esc(i.title),esc(i.taskType),esc(i.status),esc(i.description)])) : '<p>ไม่มีงานค้างตามเงื่อนไข</p>';

    const recentPreview = r.recentActivities.slice(0,5);
    const activity = recentPreview.length ? table(['ผู้ดูแลระบบ','การดำเนินการ','รายละเอียด','เวลา'],recentPreview.map(i => [esc(i.adminName||'—'),esc(i.actionType),esc(i.description||'—'),esc(i.createdAt?.replace('T',' ')||'—')])) : '<p class="muted">ยังไม่มีกิจกรรมในช่วงเวลานี้</p>';
    return head+
        dashboardCountCards([
            ['ข่าวทั้งหมด',a.total],
            ['ผู้สมัครทั้งหมด',r.applicants.total,scope+' · ทุกช่วงเวลา'],
            ['ผลที่ยังไม่เผยแพร่',t.unpublished],
            ['งานที่ต้องดำเนินการ',r.pendingTasks.total]
        ],scope)+
        `<div class="admin-dashboard-grid">${mapChart('ประเภทเนื้อหา',a.contentByType)}${mapChart('สถานะผลการรับสมัคร',t.byStatus)}</div>`+
        panel(`<h3>กิจกรรมล่าสุด</h3>${activity}`);

 }
 let adminSchoolRequest = 0;
 function validSchoolOverview(report) {
    return report && ['totalSchools','totalRegisteredTeachers','schoolsWithApplicants','currentPage','totalPages','totalItems']
        .every(key => Number.isSafeInteger(report[key]) && report[key] >= 0) &&
        Array.isArray(report.schools) && report.schools.every(s =>
            Number.isSafeInteger(s.schoolId) && s.schoolId > 0 && typeof s.schoolName === 'string' &&
            Number.isSafeInteger(s.teacherCount) && s.teacherCount >= 0 &&
            Number.isSafeInteger(s.applicantCount) && s.applicantCount >= 0);
 }
 async function loadAdminSchools(page = 0) {
    if (state.role !== 'admin') return;
    const request = ++adminSchoolRequest;
    const generation = loginGeneration;
    const current = () => request === adminSchoolRequest && generation === loginGeneration && state.role === 'admin';
    state.adminSchoolLoading = true;
    state.adminSchoolLoaded = true;
    state.adminSchoolError = '';
    if (state.page === 'school-search') render('school-search');
    try {
        const query = {page, size:10, ...state.adminSchoolFilters};
        const results = await Promise.allSettled([
            window.MfuApi.request('AdminSchoolOverviewController.getSchoolOverview', {query}),
            window.MfuApi.request('SchoolController.getAllSchools')
        ]);
        if (!current()) return;
        if (results[0].status === 'rejected') throw results[0].reason;
        const report = results[0].value;
        if (!validSchoolOverview(report)) throw new Error('รูปแบบข้อมูลโรงเรียนจาก Backend ไม่ถูกต้อง');
        state.adminSchoolReport = report;
        if (results[1].status === 'fulfilled' && Array.isArray(results[1].value)) {
            state.adminSchoolProvinces = [...new Set(results[1].value.map(s => s.subdistrict?.district?.province?.name).filter(Boolean))].sort();
        } else {
            state.adminSchoolProvinces = [...new Set([...(state.adminSchoolProvinces||[]), ...report.schools.map(s => s.province).filter(Boolean)])].sort();
        }
    } catch (error) {
        if (!current()) return;
        state.adminSchoolError = error.message || 'ไม่สามารถโหลดข้อมูลโรงเรียนได้';
    } finally {
        if (current()) {
            state.adminSchoolLoading = false;
            if (state.page === 'school-search') render('school-search');
        }
    }
 }
 function schoolAccountLabel(s) {
    return s.teacherCount === 0 ? 'ไม่มีบัญชี' : s.status === 'Active' ? 'ยืนยันอีเมลและยินยอมแล้ว' : 'รอยืนยันอีเมลหรือความยินยอม';
 }
 function showAdminSchoolDetail(id) {
    const s = state.adminSchoolReport?.schools.find(s => s.schoolId === id);
    if (!s) return;
    openModal(esc(s.schoolName), `<dl>
        <dt>รหัสโรงเรียน</dt><dd>${esc(s.schoolCode || '—')}</dd>
        <dt>จังหวัด</dt><dd>${esc(s.province || 'ไม่ระบุจังหวัด')}</dd>
        <dt>ภูมิภาค</dt><dd>${esc(s.region || '—')}</dd>
        <dt>ประเภทโรงเรียน</dt><dd>${esc(s.schoolType || '—')}</dd>
        <dt>บัญชีครูแนะแนว</dt><dd>${esc(schoolAccountLabel(s))}</dd>
        <dt>จำนวนครูแนะแนว</dt><dd>${s.teacherCount}</dd>
        <dt>จำนวนผู้สมัคร (ทุกปีการศึกษา)</dt><dd>${s.applicantCount}</dd>
    </dl>`);
 }
 function adminSchoolsPage() {
    const report = state.adminSchoolReport;
    const f = state.adminSchoolFilters || {};
    const busy = state.adminSchoolLoading;
    const provinces = [...new Set([...(state.adminSchoolProvinces || []), f.province].filter(Boolean))];
    const headingHtml = '<div class="page-heading admin-search-heading"><h1>ค้นหาข้อมูลโรงเรียน</h1></div>';
    const filters = `<form id="schoolFilter" class="filter-panel admin-filter">
        ${field('ค้นหาโรงเรียน','query',f.keyword||'','search',false,'placeholder="ค้นหาชื่อโรงเรียน..."')}
        <label>จังหวัด<select name="province"><option value="ทุกจังหวัด">ทุกจังหวัด</option>${provinces.map(p => `<option value="${esc(p)}" ${p===f.province?'selected':''}>${esc(p)}</option>`).join('')}</select></label>
        <button class="btn btn-primary" ${busy?'disabled':''}>ค้นหา</button>
    </form>`;
    if (busy) return headingHtml+filters+panel('<p role="status">กำลังโหลดข้อมูลโรงเรียน…</p>');
    if (state.adminSchoolError) return headingHtml+filters+panel(`<p role="alert">${esc(state.adminSchoolError)}</p>${btn('ลองอีกครั้ง','admin-schools-retry','btn-primary')}`);
    if (!report) return headingHtml+filters;
    const cards = '<div class="kpi-grid">'+[['โรงเรียนในระบบทั้งหมด',report.totalSchools],['โรงเรียนที่มีผู้สมัคร',report.schoolsWithApplicants],['บัญชีครูแนะแนวทั้งหมด',report.totalRegisteredTeachers]].map(([label,count]) => `<div class="panel kpi"><span>${label}</span><strong>${count}</strong><small>ข้อมูลรวมทุกปีการศึกษา</small></div>`).join('')+'</div>';
    const rows = report.schools.length ? table(['ชื่อโรงเรียน','จังหวัด','ผู้สมัคร (ทุกปี)','บัญชีครูแนะแนว','รายละเอียด'], report.schools.map(s => [
        esc(s.schoolName),esc(s.province || 'ไม่ระบุจังหวัด'),s.applicantCount,
        esc(schoolAccountLabel(s)),btn('รายละเอียด','school-detail','btn-outline',`data-id="${s.schoolId}"`)
    ])) : '<div class="empty-state">ไม่พบโรงเรียนตามเงื่อนไขที่ค้นหา</div>';
    const paging = `<div class="form-actions">
        ${btn('ก่อนหน้า','admin-schools-page','btn-outline',`data-page="${report.currentPage-1}" ${report.currentPage===0?'disabled':''}`)}
        <span>หน้า ${report.totalPages ? report.currentPage+1 : 0} / ${report.totalPages}</span>
        ${btn('ถัดไป','admin-schools-page','btn-outline',`data-page="${report.currentPage+1}" ${report.currentPage+1>=report.totalPages?'disabled':''}`)}
    </div>`;
    return headingHtml+filters+cards+`<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>รายชื่อโรงเรียน</h3><small>${report.totalItems} แห่งตรงตามเงื่อนไข · สถิติด้านบนครอบคลุมทั้งระบบ</small></div><div id="schoolRows">${rows}</div>${paging}</div>`;
 }
 const adminPageIds=['admin-dashboard','posts','expired','results','analysis','reports','school-search','admin-students','admin-applicants'];
 const adminFilters=()=>state.adminFilters||{};
 function adminScope(){const f=adminFilters();return selections.filter(s=>(f.period!=='30 วันล่าสุด'||new Date(s.appliedAt)>=new Date(Date.now()-30*864e5))&&(!f.year||s.applicant.academicYear===+f.year)&&(!f.region||f.region==='ทุกภูมิภาค'||s.applicant.school.subdistrict.district.province.region===f.region)&&(!f.round||f.round==='ทุกรอบ'||s.entryGroup.entrygroupName===f.round)&&(!f.query||(s.applicant.firstName+' '+s.applicant.lastName+' '+s.applicant.applicantId).includes(f.query)));}
 function adminInterests(){const ids=new Set(adminScope().map(s=>s.applicant.applicantId));return interests.filter(i=>ids.has(i.applicant.applicantId));}
 function adminPeriodFilter(mode){return `<form id="adminReportFilter" class="filter-panel admin-filter">${select('รอบการรับสมัคร','round',['ทุกรอบ',...entryGroups.map(g=>g.entrygroupName)])}${mode==='reports'?'':select('ช่วงเวลา','period',['ทั้งหมด','30 วันล่าสุด'])}${select('ภูมิภาค','region',mode==='reports'?['ทุกภูมิภาค','เหนือ','ใต้','ตะวันออก','ตะวันตก']:['ทุกภูมิภาค',...new Set(schools.map(s=>s.subdistrict.district.province.region))])}${mode==='reports'?'<button class="btn btn-primary">แสดงรายงาน</button>':''}${btn('ดาวน์โหลดรายงาน (CSV/PDF)','admin-export-options',mode==='analysis'?'btn-primary':'btn-outline')}</form>`;}
 function adminRanking(ints){return programs.map(p=>({p,rows:ints.filter(i=>i.program.programId===p.programId)})).sort((a,b)=>b.rows.length-a.rows.length);}
 function adminContent(page){switch(page){
 case 'admin-dashboard':return backendAdminDashboardPage();
 case 'posts':case 'expired':return `<form id="adminPostFilter" class="filter-panel admin-filter">${field('ค้นหา','query','','search',false,'placeholder="ค้นหาจากชื่อหรือคำสำคัญ"')}${select('สถานะ','visible',['ทั้งหมด','เผยแพร่แล้ว','ฉบับร่าง','ซ่อน','หมดอายุ'])}${select('ช่วงเวลาเผยแพร่','period',['ทั้งหมด','30 วันล่าสุด'])}<button class="btn btn-primary">ค้นหา</button>${btn('+ สร้างเนื้อหาใหม่','nav','btn-primary','data-target="post-new"')}</form><div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>${page==='expired'?'รายการเนื้อหาหมดอายุ':'เนื้อหาทั้งหมด'}</h3><small>${state.posts.length} รายการ</small></div><div id="postRows">${adminPostRows(page==='expired'?state.posts.filter(p=>p.visibleEndDate<new Date().toISOString().slice(0,10)):state.posts)}</div></div>`;
 case 'results':return backendAdminResultsPage();
 case 'admin-applicants':{const p=programs.find(p=>p.programId===state.adminProgramId);return heading('รายชื่อผู้สมัคร')+`<form id="adminApplicantFilter" class="filter-panel admin-filter">${select('ปีการศึกษา','year',['2569','2568'])}${select('รอบการรับสมัคร','round',['ทุกรอบ',...entryGroups.map(g=>g.entrygroupName)])}${field('ชื่อ-นามสกุล / รหัสผู้สมัคร','query')}${select('สถานะ','status',['ทุกสถานะ',...statuses.map(s=>s.description)])}<button class="btn btn-primary">ค้นหา</button></form><div class="admin-breadcrumb">${btn('‹ กลับผลการรับสมัคร','nav','text-button','data-target="results"')}</div><div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>รายชื่อผู้สมัคร: ${esc(p?.programName||'ทุกหลักสูตร')}</h3><small>ข้อมูลความสนใจในหลักสูตร</small></div><div id="adminApplicantRows">${adminApplicantRows()}</div></div>`;}
 case 'analysis':return liveAdminReportPage('analysis');
 case 'reports':return liveAdminReportPage('reports');
 case 'school-search':return adminSchoolsPage();
 case 'admin-students':return `<div class="page-heading admin-search-heading"><h1>ค้นหาข้อมูลนักเรียน</h1>${btn('ค้นหาข้อมูลโรงเรียน','nav','btn-primary','data-target="school-search"')}</div><form id="adminStudentFilter" class="filter-panel admin-filter">${field('ค้นหาชื่อ / รหัสนักเรียน','query','','search')}${select('โรงเรียน','school',['ทุกโรงเรียน',...schools.map(s=>s.name)])}<button class="btn btn-primary">ค้นหา</button></form><div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>ผลลัพธ์นักเรียน</h3></div><div id="adminStudentRows">${selectionTable(selections)}</div></div>`;
 }}
 function adminDashboardData({visible,drafts,hidden}){const f=state.dashboardFilters||{},within=p=>f.period!=='30 วันล่าสุด'||!p.publishedAt||new Date(p.publishedAt)>=new Date(Date.now()-30*864e5);let list=state.posts.filter(within);if(f.status==='ฉบับร่าง')list=list.filter(p=>p.uiDraft);if(f.status==='เผยแพร่แล้ว')list=list.filter(p=>!p.uiDraft&&!p.isHidden);if(f.type==='ผลการรับสมัคร')list=[];return kpis(['ข่าวประชาสัมพันธ์','เผยแพร่แล้ว','ฉบับร่าง','ซ่อนเนื้อหา'],[list.length,list.filter(p=>!p.uiDraft&&!p.isHidden).length,list.filter(p=>p.uiDraft).length,list.filter(p=>p.isHidden).length])+`<div class="admin-dashboard-grid">${bars('สถานะงาน Content & Results',['ข่าวประชาสัมพันธ์','ผลการรับสมัคร','พร้อมเผยแพร่','รอตรวจสอบ'],[list.length,state.datasets.length,state.datasets.filter(d=>!d.errorRows).length,state.datasets.filter(d=>d.errorRows>0).length])}${panel(`<h3>งานที่ต้องดำเนินการ</h3><div class="admin-task">${btn('ตรวจสอบเนื้อหาฉบับร่าง','nav','text-button','data-target="posts"')}<small>${drafts} รายการ</small></div><div class="admin-task">${btn('ตรวจสอบข้อมูลผลรับสมัคร','nav','text-button','data-target="results"')}<small>${state.datasets.length} ชุดข้อมูล</small></div><div class="admin-task">${btn('รายการเนื้อหาหมดอายุ','nav','text-button','data-target="expired"')}</div>`)}</div><div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>รายการอัปเดตล่าสุด</h3>${btn('ดูทั้งหมด','nav','text-button','data-target="posts"')}</div>${table(['รายการ','ประเภท','วันที่อัปเดต','สถานะ'],list.slice(0,5).map(p=>[esc(p.title),esc(p.postType),esc(p.publishedAt?.slice(0,10)||'—'),adminBadge(adminPostStatus(p))]))}</div>`;}
 function adminBadge(status){return `<span class="tag ${['เผยแพร่แล้ว','มีบัญชี','มีข้อมูลนำเข้า'].includes(status)?'status-published':status==='หมดอายุ'?'status-expired':status==='ไม่มีบัญชี'?'status-missing':'status-draft'}">${esc(status)}</span>`;}
 function adminPostStatus(p){return p.uiDraft?'ฉบับร่าง':p.isHidden?'ซ่อน':p.visibleEndDate&&p.visibleEndDate<new Date().toISOString().slice(0,10)?'หมดอายุ':'เผยแพร่แล้ว';}
 function adminPostRows(list){state.adminFilteredPosts=list;const pages=Math.max(1,Math.ceil(list.length/5));state.adminPostPage=Math.min(state.adminPostPage||1,pages);return table(['ชื่อเรื่อง','ประเภท','สถานะ','วันที่เผยแพร่','การดำเนินการ'],list.slice((state.adminPostPage-1)*5,state.adminPostPage*5).map(p=>[btn(esc(p.title),'post-detail','table-title',`data-id="${p.postId}"`),esc(p.postType),adminBadge(adminPostStatus(p)),esc(p.publishedAt?.slice(0,10)||'—'),btn('แก้ไข','post-edit','text-button',`data-id="${p.postId}"`)]))+`<div class="pagination"><span>${list.length} รายการ</span><div>${Array.from({length:pages},(_,i)=>btn(String(i+1),'admin-post-page',i+1===state.adminPostPage?'btn-primary':'btn-outline',`data-number="${i+1}"`)).join('')}</div></div>`;}
 function adminResultsData(filters={}){const source=adminScope(),list=programs.filter(p=>!filters.program||filters.program==='ทุกโครงการ / หลักสูตร'||filters.program===p.programName).filter(p=>filters.status==='มีข้อมูลนำเข้า'?state.datasets.some(d=>d.program.programId===p.programId):filters.status==='ยังไม่มีข้อมูลนำเข้า'?!state.datasets.some(d=>d.program.programId===p.programId):true);return kpis(['ชุดข้อมูลผลคัดเลือก','มีสิทธิ์สอบสัมภาษณ์','ได้รับการคัดเลือก','ชำระค่าลงทะเบียนแล้ว'],[state.datasets.length,source.filter(s=>['20','21','22','23','24','25','26','27','28'].includes(s.status.statusCode)).length,source.filter(s=>['40','41','42','43','50'].includes(s.status.statusCode)).length,source.filter(s=>s.status.statusCode==='50').length])+`<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>ชุดข้อมูลผลการคัดเลือก</h3></div>${table(['โครงการ / หลักสูตร','รอบการรับสมัคร','จำนวนข้อมูลความสนใจ','สถานะ','การดำเนินการ'],list.map(p=>[esc(p.programName),esc(filters.round||'ทุกรอบ'),adminInterests().filter(i=>i.program.programId===p.programId).length,adminBadge(state.datasets.some(d=>d.program.programId===p.programId)?'มีข้อมูลนำเข้า':'ยังไม่มีข้อมูลนำเข้า'),btn('ดูรายชื่อ','program-applicants','text-button',`data-id="${p.programId}"`)]))}</div>`;}
 function adminApplicantRows(filters={}){const ids=new Set(interests.filter(i=>i.program.programId===state.adminProgramId).map(i=>i.applicant.applicantId));const list=adminScope().filter(s=>ids.has(s.applicant.applicantId)&&(!filters.status||filters.status==='ทุกสถานะ'||filters.status===s.status.description));state.adminApplicantList=list;return adminApplicantTable();}
 function adminApplicantTable(){const list=state.adminApplicantList||[];const page=state.adminApplicantPage||1;return selectionTable(list.slice((page-1)*5,page*5))+`<div class="pagination"><span>แสดง ${list.length?(page-1)*5+1:0}–${Math.min(page*5,list.length)} จาก ${list.length} รายการ</span><div>${Array.from({length:Math.max(1,Math.ceil(list.length/5))},(_,i)=>btn(String(i+1),'admin-applicant-page',page===i+1?'btn-primary':'btn-outline',`data-number="${i+1}"`)).join('')}</div></div>`;}
 function adminAnalysisData(rank,ints,faculties){return `<div class="panel admin-ranking"><div class="admin-panel-heading"><h3>อันดับความสนใจหลักสูตร</h3><small>จัดอันดับตามจำนวนรายการความสนใจ</small></div>${rank.map((g,n)=>`<button class="rank-row" data-ui="program-applicants" data-id="${g.p.programId}"><span class="rank-number">${n+1}</span><span><strong>${esc(g.p.programName)}</strong><i style="width:${g.rows.length/Math.max(1,...rank.map(x=>x.rows.length))*100}%"></i></span><b>${g.rows.length}</b></button>`).join('')}</div><div class="admin-analysis-aside">${groupedChart('จำนวนความสนใจรวมตามสำนักวิชา',faculties,['จำนวนความสนใจ'],faculties.map(f=>[ints.filter(i=>i.program.faculty===f).length]),'faculty')}${chart('สัดส่วนรอบการรับสมัคร',entryGroups.map(g=>g.entrygroupName),entryGroups.map(g=>adminScope().filter(s=>s.entryGroup.entrygroupId===g.entrygroupId).length),'pie','round')}</div>`;}
 function adminSystemReport(){const ints=adminInterests(),rank=adminRanking(ints),source=adminScope();state.chartInterests=ints;state.chartCohort=source;return `<div class="panel admin-table-panel"><div class="admin-panel-heading"><h3>อันดับความสนใจหลักสูตร</h3></div>${table(['อันดับ','หลักสูตร','สำนักวิชา','จำนวนผู้สนใจ','สัดส่วน'],rank.map((g,n)=>[n+1,btn(esc(g.p.programName),'program-applicants','table-title',`data-id="${g.p.programId}"`),esc(g.p.faculty),g.rows.length,ints.length?(g.rows.length/ints.length*100).toFixed(1)+'%':'0.0%']))}</div><div class="admin-report-bottom">${chart('จำนวนผู้สมัครตามโรงเรียน',schools.map(s=>s.name),schools.map(s=>source.filter(x=>x.applicant.school.schoolId===s.schoolId).length),'bar','school')}${chart('สัดส่วนรอบการรับสมัคร',entryGroups.map(g=>g.entrygroupName),entryGroups.map(g=>source.filter(s=>s.entryGroup.entrygroupId===g.entrygroupId).length),'pie','round')}</div>`;}
 function adminPostForm(p={}){return `<div class="admin-editor-layout"><article class="panel admin-editor-main"><div class="admin-panel-heading"><h3>${p.postId?'แก้ไขข่าวสารและประชาสัมพันธ์':'สร้างและเผยแพร่ประชาสัมพันธ์'}</h3>${adminBadge(p.uiDraft?'ฉบับร่าง':p.postId?'แก้ไขเนื้อหา':'ฉบับร่าง')}</div><form id="postForm" class="form-grid"><input type="hidden" name="postId" value="${p.postId||''}"><label class="full">
    สรุปข่าว
    <textarea
        name="summary"
        rows="3"
        required
        placeholder="กรอกสรุปข่าวสำหรับแสดงบนการ์ด"
    >${esc(p.summary || '')}</textarea>
</label><input type="hidden" name="uiDraft" value="false"><label>
    ประเภทเนื้อหา
    <select name="postType" required>
        <option value="PR" ${(!p.postType || p.postType.toUpperCase() === 'PR') ? 'selected' : ''}>ประชาสัมพันธ์</option>
        <option value="NEWS" ${p.postType?.toUpperCase() === 'NEWS' ? 'selected' : ''}>ข่าวสาร</option>
        <option value="ANNOUNCEMENT" ${p.postType?.toUpperCase() === 'ANNOUNCEMENT' ? 'selected' : ''}>ประกาศ</option>
    </select>
</label>${select('สถานะการแสดงผล','isHidden',[p.isHidden?'ซ่อน':'แสดงบนเว็บไซต์',p.isHidden?'แสดงบนเว็บไซต์':'ซ่อน'])}<div class="full">${field('ชื่อเรื่อง','title',p.title,'text',true,'maxlength="255" placeholder="กรอกชื่อเรื่อง"')}</div><label class="full">เนื้อหา<textarea name="content" rows="5" required placeholder="รายละเอียดข่าวหรือประชาสัมพันธ์">${esc(p.content||'')}</textarea></label><div class="full">
    ${field(
        'กลุ่มเป้าหมาย',
        'targetAudience',
        p.targetAudience ?? 'ครูแนะแนว',
        'text',
        true
    )}
</div>
<div class="full">
    ${field(
        'กลุ่มเป้าหมาย',
        'targetAudience',
        p.targetAudience ?? 'ครูแนะแนว',
        'text',
        true
    )}
</div>
<div class="full">
    ${field(
        'ประเภทกิจกรรมสำหรับปฏิทิน',
        'eventType',
        p.eventType ?? '',
        'text',
        false,
        'placeholder="เว้นว่างหากไม่ต้องการแสดงในปฏิทิน"'
    )}
</div>
<h4 class="full">สถานที่จัดกิจกรรม</h4><div class="full">${field('ชื่ออาคาร / สถานที่','venue',p.venue,'text',false,'placeholder="กรอกชื่ออาคารหรือสถานที่"')}</div><div class="full admin-location-grid">${field('ตำบล / แขวง','subdistrict',p.subdistrict)}${field('อำเภอ / เขต','district',p.district)}${field('จังหวัด','province',p.province)}${field('รหัสไปรษณีย์','zipcode',p.zipcode)}</div>${field('วันเริ่มจัดกิจกรรม','eventStartDate',p.eventStartDate,'date')}${field('วันสิ้นสุดกิจกรรม','eventEndDate',p.eventEndDate,'date')}${field('วันเริ่มแสดงผล','visibleStartDate',p.visibleStartDate,'date',true)}${field('วันสิ้นสุดการแสดงผล','visibleEndDate',p.visibleEndDate,'date',true)}<div class="form-actions full admin-editor-actions">${btn('บันทึกฉบับร่าง','admin-save-draft')}${btn('ดูตัวอย่าง','preview-post')}<button class="btn btn-primary" data-publish="true">${p.postId?'บันทึกการแก้ไข':'เผยแพร่ทันที'}</button></div></form></article><aside class="panel admin-preview-panel"><div class="admin-panel-heading"><h3>ตัวอย่างการแสดงผล</h3></div><div id="postPreview"><span class="tag tag-orange">${esc(p.postType||'ข่าว PR')}</span><h2>${esc(p.title||'ชื่อเรื่องประชาสัมพันธ์')}</h2><p>${esc(p.summary||p.content||'รายละเอียดข่าวหรือประชาสัมพันธ์')}</p></div><div class="admin-checklist"><h3>รายการตรวจสอบก่อนเผยแพร่</h3><ul><li>กรอกชื่อเรื่องและเนื้อหาครบแล้ว</li><li>กำหนดช่วงเวลาแล้ว</li><li>กำหนดสถานะการแสดงผลแล้ว</li><li>ตรวจสอบตัวอย่างแล้ว</li></ul></div></aside></div>`;}
 function adminPostDetail(id){document.querySelectorAll('.nav-link').forEach(b=>b.classList.toggle('active',b.dataset.target==='posts'));const p=state.posts.find(p=>p.postId===+id);if(!p)return;state.page='project-detail';state.selectedPost=p.postId;get('extendedPage').dataset.view='admin-post-detail';get('extendedPage').innerHTML=`<div class="admin-breadcrumb">${btn('ข่าวสารและประชาสัมพันธ์','nav','text-button','data-target="posts"')} / รายละเอียดข่าว</div><div class="page-heading"><h1>รายละเอียดข่าว: ${esc(p.title)}</h1>${adminBadge(adminPostStatus(p))}</div><div class="admin-detail-layout">${panel(`<h3>ข้อมูลข่าวประชาสัมพันธ์</h3><dl class="review-grid one-column"><div><dt>ชื่อเรื่อง</dt><dd>${esc(p.title)}</dd></div><div><dt>ประเภท</dt><dd>${esc(p.postType)}</dd></div><div><dt>วันที่เผยแพร่</dt><dd>${esc(p.publishedAt?.slice(0,10)||'—')}</dd></div><div><dt>ช่วงแสดงผล</dt><dd>${esc(p.visibleStartDate)} – ${esc(p.visibleEndDate)}</dd></div></dl><div class="detail-copy preserved">${esc(p.content)}</div>${p.venue?`<p>สถานที่: ${esc(p.venue)}</p>`:''}`)}${panel(`<h3>ตรวจสอบก่อนเผยแพร่</h3><ul class="admin-checklist"><li>ชื่อเรื่องและเนื้อหาครบถ้วน</li><li>กำหนดช่วงแสดงผลแล้ว</li><li>ตรวจสอบตัวอย่างแล้ว</li></ul><div class="form-actions">${btn('แก้ไขข่าว','post-edit','btn-outline',`data-id="${p.postId}"`)}${btn(p.isHidden?'แสดงเนื้อหา':'ซ่อนเนื้อหา','admin-hide-post','btn-primary',`data-id="${p.postId}"`)}</div>`)}</div>`;}

 document.addEventListener('input',e=>{if(state.role==='admin'&&e.target.closest('#postForm')){const f=get('postForm');get('postPreview').innerHTML=`<span class="tag tag-orange">${esc(f.elements.postType.value)}</span><h2>${esc(f.elements.title.value||'ชื่อเรื่องประชาสัมพันธ์')}</h2><p>${esc(f.elements.content.value||'รายละเอียดข่าวหรือประชาสัมพันธ์')}</p><small>${esc(f.elements.visibleStartDate.value)} – ${esc(f.elements.visibleEndDate.value)}</small>`;}});
 // Reference-only registration states. Existing Login and application layouts are unchanged.
 const authEye='<svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
 function referenceField(label,name,value='',type='text',placeholder=''){return `<label>${label}<input name="${name}" type="${type}" value="${esc(value)}" placeholder="${esc(placeholder)}" required ${type==='password'?'minlength="8" autocomplete="new-password"':''} ${type==='email'?'autocomplete="email"':''}></label>`;}
 function fitRegistrationCard(){const root=get('authRoot');if(!root?.classList.contains('auth-reference')||root.classList.contains('hidden'))return;const card=root.querySelector('.reference-auth-card');if(card)card.style.transform=`scale(${Math.min(1,Math.max(0.1,(root.clientHeight-32)/card.offsetHeight))})`;}
 window.addEventListener('resize',fitRegistrationCard);
 function referenceAuth(view){document.body.classList.add('auth-mode');document.querySelector('.app-shell').classList.add('hidden');const root=get('authRoot');root.classList.remove('hidden');root.classList.add('auth-reference');root.dataset.authStep=view;const draft=state.registrationDraft||{};const subtitle='สำหรับโรงเรียนเครือข่ายครูแนะแนว มหาวิทยาลัยแม่ฟ้าหลวง';const loginLine=`<div class="reference-login">มีบัญชีอยู่แล้ว? ${btn('เข้าสู่ระบบ','auth-login','reference-link')}</div>`;const support=`<p class="reference-support">กรณีไม่สามารถเข้าใช้งานระบบได้ กรุณาติดต่อฝ่ายรับนักศึกษา ส่วนทะเบียนและประมวลผล<br><a href="tel:053916103">โทร. 0 5391 6103</a></p>`;let body='';
 if(view==='register'){body=registrationFormMarkup()+loginLine;}
else if(view==='password'){
 const isReset = !!state.passwordResetToken;
 body=`<h1>${isReset?'ตั้งรหัสผ่านใหม่':'ตั้งรหัสผ่านเพื่อเข้าใช้งานระบบครูแนะแนว'}</h1>
 <p class="reference-subtitle">${subtitle}</p>
 <form id="authForm" class="reference-form">
 <div class="reference-row">
 <div class="reference-password">
 ${referenceField(isReset?'รหัสผ่านใหม่':'ตั้งรหัสผ่าน','password','','password','กรอกรหัสผ่านอย่างน้อย 8 ตัวอักษร')}
 <button type="button" data-ui="auth-toggle-secret" data-input="password" aria-label="แสดงรหัสผ่าน">${authEye}</button>
 </div>
 <div class="reference-password">
 ${referenceField('ยืนยันรหัสผ่าน','confirm','','password','กรอกรหัสผ่านอีกครั้ง')}
 <button type="button" data-ui="auth-toggle-secret" data-input="confirm" aria-label="แสดงยืนยันรหัสผ่าน">${authEye}</button>
 </div>
 </div>
 ${isReset?'':`<div class="reference-consent"><h2>หนังสือแสดงความยินยอม (Consent Form) การเก็บข้อมูลครูแนะแนว</h2><label><input name="pdpaConsent" type="checkbox" required> <span>ข้าพเจ้ายินยอมให้เก็บและใช้ข้อมูลตามพระราชบัญญัติคุ้มครองข้อมูลส่วนบุคคล (PDPA)</span></label>${btn('อ่านรายละเอียดเพิ่มเติม','registration-consent','reference-link')}</div>`}
 ${isReset?'':btn('← ย้อนกลับเพื่อแก้ไขข้อมูล','registration-back','btn-outline')}
 <button class="btn btn-primary reference-submit">${isReset?'บันทึกรหัสผ่านใหม่':'ยินยอมและยื่นคำขอ'}</button>
 </form>
 ${loginLine}`;
} else if(view==='sent'){body=`<div class="reference-status-icon sent-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><rect x="3" y="5" width="17" height="14" rx="2"/><path d="m3 6 8.5 7L20 6M17 2l2 2 3-3"/></svg></div><h1>ส่งคำขอสำเร็จ</h1><p class="reference-message">กรุณายืนยันตัวตนผ่านลิงก์ที่ระบบส่งไปยังอีเมลของท่านเพื่อเริ่มใช้งานระบบ</p><div class="reference-rule"></div><div class="reference-note"><strong>หมายเหตุ:</strong><p>หากไม่พบบันทึกข้อความยืนยันในกล่องจดหมายเข้า กรุณาตรวจสอบในแฟ้ม<br>Trash หรือ Spam Mail หากยังไม่ได้รับอีเมล กรุณาติดต่อฝ่ายสนับสนุน</p></div>${support}`;}
 else{body=`<div class="reference-status-icon confirmed-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20 11a8 8 0 1 1-4-6"/><path d="m8 11 4 4 8-10"/></svg></div><h1>ยืนยันอีเมลเรียบร้อย</h1><p class="reference-message">การสมัครสมาชิกเสร็จสมบูรณ์เรียบร้อยแล้ว<br>กรุณาเข้าสู่ระบบด้วยบัญชีผู้ใช้ที่ลงทะเบียนเพื่อเริ่มงาน</p><div class="reference-rule"></div><div class="reference-account-status"><span>สถานะบัญชี</span><strong>เปิดใช้งานแล้ว (Active)</strong></div>${btn('เข้าสู่ระบบ','auth-login','btn-primary reference-submit')}${support}`;}
 root.innerHTML=`<section class="auth-card reference-auth-card" aria-label="${view==='register'?'ลงทะเบียนครูแนะแนว':view==='password'?'ตั้งรหัสผ่าน':view==='sent'?'ส่งคำขอสำเร็จ':'ยืนยันอีเมล'}">${body}</section>`;if(view==='register')restoreRegistrationDraft();fitRegistrationCard();document.fonts.ready.then(fitRegistrationCard);window.scrollTo(0,0);}

 function init(){state.pins=read('pins.school-'+(state.profile.schoolId||1),[]);document.querySelector('.main-area').insertAdjacentHTML('beforeend','<section id="extendedPage" class="page hidden"></section>');document.querySelector('.footer').remove();document.body.insertAdjacentHTML('beforeend','<main id="authRoot" class="hidden"></main><dialog id="uiDialog" aria-label="รายละเอียดและยืนยันการดำเนินการ"></dialog>');document.querySelector('.notification').innerHTML='<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>';document.querySelector('.notification').onclick=()=>toast('ไม่มีการแจ้งเตือนใหม่ในตัวอย่าง');document.querySelector('.brand').removeAttribute('data-page');document.querySelector('.brand').onclick=e=>{e.preventDefault();render(state.role==='admin'?'admin-dashboard':'dashboard');};const params = new URLSearchParams(window.location.search);
const resetToken = params.get('token');

if (resetToken) {
    state.passwordResetToken = resetToken;
    auth('password', 'teacher');
} else {
    let pending = null;
    try {pending = JSON.parse(sessionStorage.getItem('mfu.registrationCheck'));} catch {}
    if (typeof pending?.token === 'string' && pending.expiresAt > Date.now()) {
        registrationCheckToken = pending.token;
        auth('sent', 'teacher');
    } else {
        auth();
    }
}}
 init();
 return {state,render,auth,fixtures:{get applicants(){return scope(applicants)},get selections(){return scope(selections)},get interests(){return scope(interests)},programs,get schools(){return state.role==='admin'?schools:[ownSchool()]},entryGroups,statuses},esc};
})();
