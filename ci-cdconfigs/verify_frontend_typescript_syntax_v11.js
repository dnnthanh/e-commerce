const ts=require('typescript'),fs=require('fs'),path=require('path');
function walk(d){return fs.readdirSync(d,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(d,e.name)):(e.name.endsWith('.ts')?[path.join(d,e.name)]:[]));}
const files=walk(path.resolve(__dirname,'..','frontend'));let errors=[];
for(const f of files){const out=ts.transpileModule(fs.readFileSync(f,'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ESNext},reportDiagnostics:true,fileName:f});for(const d of out.diagnostics||[]){if(d.category===ts.DiagnosticCategory.Error)errors.push(`${path.relative(process.cwd(),f)}: ${ts.flattenDiagnosticMessageText(d.messageText,' ')}`);}}
if(errors.length){console.log('FRONTEND_TYPESCRIPT_SYNTAX_V11=FAIL');for(const e of errors)console.log('-',e);process.exit(1);}console.log(`FRONTEND_TYPESCRIPT_SYNTAX_V11=PASS files=${files.length}`);
