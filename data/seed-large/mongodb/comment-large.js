db=db.getSiblingDB('comment_db');
const contentTemplates=[
  'Sản phẩm này dùng thực tế pin được khoảng bao lâu?',
  'Mình cần xuất hóa đơn công ty, shop hỗ trợ không?',
  'Phiên bản này có tương thích phụ kiện đời trước không?',
  'Đã dùng vài tuần, hiệu năng ổn và giao hàng đúng hẹn.',
  'Shop tư vấn giúp mình khác nhau giữa hai biến thể với.',
  'Có hỗ trợ bảo hành tại trung tâm ở Hà Nội/HCM không?',
  'Mình đặt hôm qua, trạng thái đơn cập nhật khá nhanh.',
  'Chất lượng hoàn thiện tốt hơn mình kỳ vọng trong tầm giá.'
];
const hotProductRatio=8;      // 8% hot products receive a disproportionate share of comments.
const moderationRatio=5;     // per thousand hidden/deleted examples.
let batch=[];
for(let i=1;i<=1000000;i++){
  const hot=(i%100)<hotProductRatio;
  const productId=hot ? 1000001+(i%50000) : 1000001+((i*7919)%500000);
  const userBucket=(i%100<18)?(i%5000):(5000+(i%245000));
  const mod=i%1000;
  const status=mod<2?'HIDDEN':(mod<moderationRatio?'DELETED':(i%19===0?'EDITED':'PUBLISHED'));
  const replies=hot ? (i%18) : (i%5===0?1:0);
  batch.push({insertOne:{document:{
    _id:'CMT-L-'+i,productId,sellerId:10001+(i%500),authorId:'customer-'+String(1+userBucket).padStart(7,'0'),
    content:contentTemplates[i%contentTemplates.length],status,replyCount:replies,
    createdAt:new Date(Date.now()-(i%365)*86400000-((i%24)*3600000)),updatedAt:new Date()
  }}});
  if(batch.length===1000){db.comment_thread.bulkWrite(batch,{ordered:false});batch=[];}
}
if(batch.length)db.comment_thread.bulkWrite(batch,{ordered:false});
