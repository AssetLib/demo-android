import { readFileSync, writeFileSync, mkdirSync, existsSync, renameSync, rmSync } from 'node:fs';
import { createHash } from 'node:crypto';
const release=JSON.parse(readFileSync(new URL('../sdk-release.json',import.meta.url)));
if(release.version!=='0.1.0-preview.1'||!/^https:\/\/github\.com\/AssetLib\/sdk-android\/releases\/download\/v0\.1\.0-preview\.1\/assetlib-android-0\.1\.0-preview\.1\.aar$/.test(release.url)||!/^[a-f0-9]{64}$/.test(release.sha256)) throw Error('Invalid release lock.');
const file=new URL('../app/libs/assetlib-android-0.1.0-preview.1.aar',import.meta.url);
const valid=b=>b.byteLength<=10*1024*1024&&createHash('sha256').update(b).digest('hex')===release.sha256;
if(existsSync(file)) { if(!valid(readFileSync(file))) throw Error('Existing SDK AAR hash mismatch. Remove the file and retry.'); }
else {
 const response=await fetch(release.url,{signal:AbortSignal.timeout(30000)}); if(!response.ok) throw Error(`SDK release download returned ${response.status}. For unreleased local development use -PassetlibSdkDir=../sdk-android.`);
 const chunks=[]; let size=0; for await(const chunk of response.body) { size+=chunk.byteLength; if(size>10*1024*1024) throw Error('SDK artifact exceeds limit.'); chunks.push(chunk); }
 const bytes=Buffer.concat(chunks); if(!valid(bytes)) throw Error('SDK release SHA-256 mismatch.');
 mkdirSync(new URL('../app/libs/',import.meta.url),{recursive:true}); const temp=new URL(file.href+'.part');
 try { writeFileSync(temp,bytes); renameSync(temp,file); } finally { rmSync(temp,{force:true}); }
}
console.log(`Verified Assetlib Android ${release.version}.`);
