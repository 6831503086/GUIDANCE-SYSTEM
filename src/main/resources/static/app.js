/* MFU Guidance frontend prototype.
   Demo content is intentionally isolated here and must be replaced by API data. */
const projects = [
  {
    id: 1, type: "ประชาสัมพันธ์", status: "กำลังประชาสัมพันธ์",
    title: "ประชาสัมพันธ์โครงการสำหรับครูแนะแนว",
    description: "ข่าวสารและข้อมูลโครงการสำหรับครูแนะแนว เพื่อกระชับความร่วมมือและชี้แจงสิทธิประโยชน์ต่าง ๆ แก่เครือข่ายสถานศึกษา",
    announced: "15 กันยายน 2569", period: "15 กันยายน 2569 – 30 กันยายน 2569",
    department: "Admissions Office", phone: "053-916000",
    attachment: "คู่มือและรายละเอียดโครงการครูแนะแนว_2569.pdf"
  },
  {
    id: 2, type: "ประชาสัมพันธ์", status: "กำลังรับสมัคร",
    title: "เปิดรับสมัครนักศึกษาใหม่ในรอบ Quota ภาคเหนือ ประจำปีการศึกษา 2569",
    description: "รายละเอียดแนวทางการคัดเลือกและโควตาพิเศษเชียงใหม่ 17 จังหวัดภาคเหนือ สำหรับผู้สนใจศึกษาต่อระดับอุดมศึกษา",
    announced: "10 กันยายน 2569", period: "10 กันยายน 2569 – 15 ตุลาคม 2569",
    department: "MFU Admissions Office", phone: "053-916000",
    attachment: "ระเบียบการรับสมัคร_Quota69.pdf"
  },
  {
    id: 3, type: "ทุนการศึกษา", status: "กำลังประชาสัมพันธ์",
    title: "ทุนการศึกษาเรียนดีแต่ขาดแคลนทุนทรัพย์ ประจำปีการศึกษา 2569",
    description: "ทุนช่วยเหลือนักเรียนที่เรียนดีและขาดแคลนทุนทรัพย์ สำหรับนักเรียนเครือข่ายแนะแนวที่ผ่านเกณฑ์การพิจารณาเป็นพิเศษ",
    announced: "05 กันยายน 2569", period: "05 กันยายน 2569 – 30 ตุลาคม 2569",
    department: "Student Financial Aid Unit", phone: "053-916000",
    attachment: "ใบสมัครทุนการศึกษา.pdf"
  },
  {
    id: 4, type: "กิจกรรม", status: "สิ้นสุดโครงการ",
    title: "MFU Open House 2026: มหกรรมเปิดบ้านวิชาการต้อนรับโรงเรียนเครือข่าย",
    description: "กิจกรรมแนะแนวเชิงนโยบายเยี่ยมชมศูนย์ปฏิบัติการทางการแพทย์ เทคโนโลยีสารสนเทศ และการจำลองการเรียนทางวิชาชีพ",
    announced: "25 สิงหาคม 2569", period: "01 กันยายน 2569 – 10 กันยายน 2569",
    department: "Academic Affairs Division", phone: "053-916000",
    attachment: "กำหนดการและแผนผัง_OpenHouse2026.pdf"
  }
];

const demoApplicants = [
  ["AP-2569-001", "นางสาวตัวอย่าง ใจดี", "โครงการ Quota", "ยืนยันแล้ว"],
  ["AP-2569-002", "นายตัวอย่าง เรียนดี", "ทุนการศึกษา", "รอตรวจสอบ"],
  ["AP-2569-003", "นางสาวตัวอย่าง มุ่งมั่น", "โครงการ Quota", "ยืนยันแล้ว"]
];

const $ = (selector) => document.querySelector(selector);
const toast = (message) => {
  const node = $("#toast");
  node.textContent = message;
  node.classList.add("show");
  window.setTimeout(() => node.classList.remove("show"), 2600);
};

function projectCard(project) {
  const tagClass = project.type === "กิจกรรม" ? "tag-blue" : project.type === "ทุนการศึกษา" ? "tag-orange" : "tag-blue";
  const statusClass = project.status === "สิ้นสุดโครงการ" ? "tag-gray" : "tag-green";
  return `<article class="project-card">
    <div class="tag-row"><span class="tag ${tagClass}">${project.type}</span><span class="tag ${statusClass}">${project.status}</span></div>
    <h2>${project.title}</h2>
    <p class="description">${project.description}</p>
    <div class="project-meta">วันที่ประกาศ: ${project.announced}<strong class="project-period">ช่วงเวลาประชาสัมพันธ์: ${project.period}</strong>
      <button class="btn btn-primary btn-block" data-project="${project.id}">อ่านรายละเอียดเพิ่มเติม</button>
    </div>
  </article>`;
}

function renderProjects(target, list) {
  $(target).innerHTML = list.length ? list.map(projectCard).join("") : `<div class="panel empty-state"><strong>ไม่พบโครงการ</strong><p>ลองเปลี่ยนคำค้นหาหรือเงื่อนไขอีกครั้ง</p></div>`;
}

function openProject(id) {
  const p = projects.find(item => item.id === Number(id));
  if (!p) return;
  $("#projectDetail").innerHTML = `
    <div class="tag-row"><span class="tag tag-blue">${p.type}</span><span class="tag tag-green">${p.status}</span></div>
    <h1>${p.title}</h1>
    <div class="detail-date">วันที่ประกาศ: ${p.announced}<strong>▣ ช่วงเวลาประชาสัมพันธ์: ${p.period}</strong></div>
    <div class="detail-copy"><h3>รายละเอียดโครงการ</h3><p>${p.description}</p></div>
    <div class="attachment"><strong>เอกสารแนบ</strong><div class="attachment-row"><span>▤ &nbsp;${p.attachment}</span><a href="#" data-download="${p.id}">ดาวน์โหลด ↓</a></div></div>`;
  $("#projectAction").textContent = p.status === "สิ้นสุดโครงการ" ? "โครงการสิ้นสุดแล้ว" : "ตอบรับเข้าร่วมโครงการ / ลงทะเบียน";
  $("#projectAction").disabled = p.status === "สิ้นสุดโครงการ";
  showPage("project-detail");
}

function showPage(page) {
  document.querySelectorAll(".page").forEach(el => el.classList.add("hidden"));
  const target = $(`#page-${page}`);
  if (target) target.classList.remove("hidden");
  document.querySelectorAll(".nav-link").forEach(btn => btn.classList.toggle("active", btn.dataset.page === page || (page === "project-detail" && btn.dataset.page === "dashboard")));
  $("#sidebar").classList.remove("open");
  if (page === "overview") renderCharts();
}

document.addEventListener("click", (event) => {
  const nav = event.target.closest("[data-page]");
  if (nav) { event.preventDefault(); showPage(nav.dataset.page); }
  const projectButton = event.target.closest("[data-project]");
  if (projectButton) openProject(projectButton.dataset.project);
  const download = event.target.closest("[data-download]");
  if (download) { event.preventDefault(); toast("ไฟล์ตัวอย่างยังไม่ได้แนบ — รอเชื่อมต่อไฟล์จาก Backend"); }
});

$("#mobileMenu").addEventListener("click", () => $("#sidebar").classList.toggle("open"));
$("#filterProjects").addEventListener("click", () => {
  const type = $("#projectType").value;
  const status = $("#projectStatus").value;
  const query = $("#projectSearch").value.trim().toLowerCase();
  renderProjects("#allProjects", projects.filter(p =>
    (!type || p.type === type) && (!status || p.status === status) &&
    (!query || p.title.toLowerCase().includes(query) || p.description.toLowerCase().includes(query))
  ));
});
$("#searchApplicants").addEventListener("click", () => {
  const q = $("#applicantSearch").value.trim().toLowerCase();
  const rows = demoApplicants.filter(row => row.join(" ").toLowerCase().includes(q));
  $("#applicantRows").innerHTML = rows.map(row => `<tr><td>${row[0]}</td><td>${row[1]}</td><td>${row[2]}</td><td class="status">${row[3]}</td></tr>`).join("") || `<tr><td colspan="4">ไม่พบข้อมูลในชุดตัวอย่าง</td></tr>`;
});

$("#projectAction").addEventListener("click", () => toast("ส่วนลงทะเบียนจะเปิดใช้งานเมื่อเชื่อมต่อ Backend"));
renderProjects("#dashboardProjects", projects);
renderProjects("#allProjects", projects);
$("#applicantRows").innerHTML = demoApplicants.map(row => `<tr><td>${row[0]}</td><td>${row[1]}</td><td>${row[2]}</td><td class="status">${row[3]}</td></tr>`).join("");

let calendarDate = new Date(2026, 8, 1);
function renderCalendar() {
  const year = calendarDate.getFullYear(), month = calendarDate.getMonth();
  $("#calendarMonth").textContent = new Intl.DateTimeFormat("th-TH", {month:"long", year:"numeric"}).format(calendarDate);
  const first = new Date(year, month, 1).getDay();
  const days = new Date(year, month + 1, 0).getDate();
  const names = ["อา.","จ.","อ.","พ.","พฤ.","ศ.","ส."];
  let html = names.map(n => `<div class="calendar-cell day-name">${n}</div>`).join("");
  for (let i=0; i<first; i++) html += `<div class="calendar-cell"></div>`;
  for (let day=1; day<=days; day++) {
    const event = (month === 8 && day === 15) ? `<span class="calendar-event">ประชาสัมพันธ์</span>` : (month === 8 && day === 25) ? `<span class="calendar-event">กิจกรรม</span>` : "";
    html += `<div class="calendar-cell">${day}${event}</div>`;
  }
  $("#calendarGrid").innerHTML = html;
}
$("#prevMonth").addEventListener("click", () => { calendarDate.setMonth(calendarDate.getMonth()-1); renderCalendar(); });
$("#nextMonth").addEventListener("click", () => { calendarDate.setMonth(calendarDate.getMonth()+1); renderCalendar(); });
renderCalendar();

let chartsCreated = false;
function renderCharts() {
  if (chartsCreated || !window.Chart) return;
  const common = {responsive:true, maintainAspectRatio:false, plugins:{legend:{labels:{font:{family:"Sarabun"}}}}};
  new Chart($("#barChart"), {type:"bar", data:{labels:[], datasets:[{label:"จำนวนผู้สมัคร", data:[], backgroundColor:"#7A2222", borderRadius:5}]}, options:{...common, scales:{y:{beginAtZero:true, ticks:{precision:0}}}}});
  new Chart($("#doughnutChart"), {type:"doughnut", data:{labels:[], datasets:[{data:[], backgroundColor:["#7A2222","#C9A227","#D97706"]}]}, options:common});
  chartsCreated = true;
}
